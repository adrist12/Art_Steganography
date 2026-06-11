/**
 * Utilidades para WebAuthn
 * Maneja la comunicación con la API Java y el navegador
 */

// Función auxiliar para convertir JSON a ArrayBuffer (necesario para la API WebAuthn)
function base64UrlToBuffer(base64url) {
    const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    const padLength = (4 - (base64.length % 4)) % 4;
    const padded = base64.padEnd(base64.length + padLength, '=');
    const binary = atob(padded);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
}

// Función auxiliar para convertir ArrayBuffer a Base64URL (para enviar al backend)
function bufferToBase64Url(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

/**
 * 1. REGISTRO DE CREDENCIAL
 * Llama al backend, pide datos al navegador y envía la respuesta.
 */
async function registerCredential(email, nombre) {
    try {
        // Paso 1: Obtener las opciones de registro del backend
        const optionsResponse = await fetch(`/webauthn/register/options?email=${encodeURIComponent(email)}&nombre=${encodeURIComponent(nombre)}`);
        if (!optionsResponse.ok) throw new Error("Error al obtener opciones de registro");

        const creationOptionsJSON = await optionsResponse.json();

        // Paso 2: Ajustar los IDs (el backend los envía en Base64, el navegador necesita ArrayBuffer)
        const publicKey = {
            ...creationOptionsJSON,
            challenge: base64UrlToBuffer(creationOptionsJSON.challenge),
            user: {
                ...creationOptionsJSON.user,
                id: base64UrlToBuffer(creationOptionsJSON.user.id)
            },
            excludeCredentials: creationOptionsJSON.excludeCredentials?.map(cred => ({
                ...cred,
                id: base64UrlToBuffer(cred.id)
            }))
        };

        // Paso 3: Pedir al navegador que cree la credencial (Huella/FaceID/Llave)
        const credential = await navigator.credentials.create({ publicKey });

        // Paso 4: Preparar la respuesta para enviar al backend
        const attestationResponse = {
            id: credential.id,
            rawId: bufferToBase64Url(credential.rawId),
            type: credential.type,
            response: {
                attestationObject: bufferToBase64Url(credential.response.attestationObject),
                clientDataJSON: bufferToBase64Url(credential.response.clientDataJSON)
            }
        };

        // Paso 5: Enviar la respuesta al backend para guardar
        const finishResponse = await fetch('/webauthn/register/finish', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(attestationResponse)
        });

        if (!finishResponse.ok) {
            const error = await finishResponse.text();
            throw new Error(error || "Error al finalizar registro");
        }

        return { success: true, message: "¡Credencial registrada con éxito!" };

    } catch (error) {
        console.error("Error en registro WebAuthn:", error);
        throw error;
    }
}

/**
 * 2. LOGIN CON CREDENCIAL
 * Llama al backend, pide autenticación y valida la sesión.
 */
async function loginCredential(email) {
    try {
        // Paso 1: Obtener las opciones de login (challenge) del backend
        const optionsResponse = await fetch(`/webauthn/login/options?email=${encodeURIComponent(email)}`);
        if (!optionsResponse.ok) throw new Error("Error al obtener opciones de login");

        const assertionOptionsJSON = await optionsResponse.json();

        // Paso 2: Ajustar los IDs a ArrayBuffer
        const publicKey = {
            ...assertionOptionsJSON,
            challenge: base64UrlToBuffer(assertionOptionsJSON.challenge),
            allowCredentials: assertionOptionsJSON.allowCredentials?.map(cred => ({
                ...cred,
                id: base64UrlToBuffer(cred.id)
            }))
        };

        // Paso 3: Pedir al navegador que firme (Huella/FaceID/Llave)
        const assertion = await navigator.credentials.get({ publicKey });

        // Paso 4: Preparar la respuesta para el backend
        const assertionResponse = {
            id: assertion.id,
            rawId: bufferToBase64Url(assertion.rawId),
            type: assertion.type,
            response: {
                authenticatorData: bufferToBase64Url(assertion.response.authenticatorData),
                clientDataJSON: bufferToBase64Url(assertion.response.clientDataJSON),
                signature: bufferToBase64Url(assertion.response.signature),
                userHandle: assertion.response.userHandle ? bufferToBase64Url(assertion.response.userHandle) : null
            }
        };

        // Paso 5: Enviar al backend para verificar y crear sesión
        const finishResponse = await fetch('/webauthn/login/finish', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(assertionResponse)
        });

        if (!finishResponse.ok) {
            const error = await finishResponse.text();
            throw new Error(error || "Error al finalizar login");
        }

        const result = await finishResponse.json();

        // Redirigir si el backend lo indica
        if (result.redirectUrl) {
            window.location.href = result.redirectUrl;
        }

        return { success: true, message: "¡Login exitoso!" };

    } catch (error) {
        console.error("Error en login WebAuthn:", error);
        throw error;
    }
}