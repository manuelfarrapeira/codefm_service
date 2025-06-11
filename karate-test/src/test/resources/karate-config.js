function fn() {
    var config = {};


    try {
        var env = karate.readAsString('classpath:.env.secret');
        env.split('\n').forEach(function (line) {
            if (line.trim()) {
                var parts = line.split('=');
                if (parts[0] === 'TOKEN_INFISICAL') {
                    config.infisicalToken = parts[1];
                }
            }
        });

    } catch (e) {
        karate.log('.env.secret no encontrado o error al leerlo, se continúa sin TOKEN_INFISICAL');
    }


    config.baseHttpsUrl = 'https://codefm.synology.me:5553';
    config.baseHttpUrl = 'http://example.com';


    var secrets = karate.callSingle('classpath:features/authenticate/infisical.feature', {token: config.infisicalToken});


    for (var key in secrets) {
        config[key] = secrets[key];
    }

    return config;
}