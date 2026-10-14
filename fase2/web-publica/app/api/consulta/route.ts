import { NextResponse } from 'next/server';
import { writeFileSync } from 'fs';
import { redirect } from 'next/navigation';

export const config = {
  api: {
    bodyParser: true,
  },
};

export async function POST(request) {
  const body = await request.json();
  const { codigo, token } = body;

  // 1. Validar que codigo y token existan
  if (!codigo || !token) {
    return NextResponse.json(
      { mensaje: 'Faltan campos requeridos: codigo y token' },
      { status: 400 }
    );
  }

  // 2. Validar formato del código (mismo patrón que OpenAPI)
  const patronCodigo = /^[A-Z0-9_-]{1,20}$/;
  if (!patronCodigo.test(codigo)) {
    return NextResponse.json(
      { mensaje: 'El código del contador tiene formato inválido' },
      { status: 400 }
    );
  }

  // 3. Verificar captcha con Google
  const secretoRecaptcha = process.env.RECAPTCHA_SECRET_KEY;
  if (!secretoRecaptcha) {
    return NextResponse.json(
      { mensaje: 'Configuración del captcha incompleta' },
      { status: 500 }
    );
  }

  const urlVerificacion = 'https://www.google.com/recaptcha/api/siteverify';
  const params = new URLSearchParams({
    secret: secretoRecaptcha,
    response: token,
  });

  try {
    const captchaResponse = await fetch(urlVerificacion, {
      method: 'POST',
      body: params,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    const resultadoCaptcha = await captchaResponse.json();

    if (!resultadoCaptcha.success) {
      return NextResponse.json(
        { mensaje: 'Captcha inválido o vencido' },
        { status: 400 }
      );
    }
  } catch (error) {
    return NextResponse.json(
      { mensaje: 'Error al verificar el captcha con el servicio de Google' },
      { status: 500 }
    );
  }

  // 4. Llamar al microservicio con la IP real del cliente
  const serviceUrl = process.env.CONSULTA_SERVICE_URL || 'http://localhost:8081';
  const ipCliente = request.headers.get('x-forwarded-for')?.split(',')[0] || 'unknown';

  try {
    const servicioResponse = await fetch(`${serviceUrl}/api/v1/consulta/${encodeURIComponent(codigo)}`, {
      method: 'GET',
      headers: {
        'X-Forwarded-For': ipCliente,
        'Accept': 'application/json',
      },
      cache: 'no-store',
    });

    // Traducir la respuesta del microservicio
    const data = await servicioResponse.json();

    if (servicioResponse.status === 200) {
      return NextResponse.json(data, { status: 200 });
    }

    if (servicioResponse.status === 404) {
      return NextResponse.json(
        { mensaje: data.mensaje || 'No existe un contador con ese código' },
        { status: 404 }
      );
    }

    if (servicioResponse.status === 429) {
      return NextResponse.json(
        { mensaje: data.mensaje || 'Demasiadas peticiones. Inténtalo más tarde' },
        { status: 429 }
      ).headers.set('Retry-After', data.retryAfter || '60');
    }

    // 5xx o respuestas inesperadas del microservicio
    return NextResponse.json(
      { mensaje: 'El servicio de consulta no está disponible en este momento' },
      { status: 502 }
    );

  } catch (error) {
    // Error de conexión o microservicio no responde
    return NextResponse.json(
      { mensaje: 'No se pudo conectar con el servicio de consulta' },
      { status: 502 }
    );
  }
}