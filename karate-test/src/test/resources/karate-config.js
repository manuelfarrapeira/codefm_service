function fn() {
    var config = {};


    try {

        var System = Java.type('java.lang.System');
        var token = System.getenv('INFISICAL_TOKEN');

        if (token && token.trim()) {
            config.infisicalToken = token.trim();
        } else {
            var env = karate.readAsString('classpath:.env.secret');
            env.split('\n').forEach(function (line) {
                if (line.trim()) {
                    var parts = line.split('=');
                    if (parts[0] === 'INFISICAL_TOKEN') {
                        config.infisicalToken = parts[1];
                    }
                }
            });
            karate.log("INFISICAL_TOKEN cargado desde .env.secret");
        }

    } catch (e) {
        karate.log('.env.secret no encontrado o error al leerlo, se continúa sin TOKEN_INFISICAL');
    }

    config.baseHttpsUrl = 'https://codefm.synology.me:5553';
    config.baseHttpUrl = 'http://codefm.synology.me:8083';
    config.baseLocalUrl = 'http://localhost:8081';


    var secrets = karate.callSingle('classpath:features/common/infisical.feature', {token: config.infisicalToken});

    for (var key in secrets) {
        config[key] = secrets[key];
    }


    var users = [
        {username: 'codefm', password: config['codefm']},
        {username: 'karateuseradmin', password: config['karateuseradmin']}
    ];


    config.authTokens = {};

    users.forEach(function (user) {
        var result = karate.callSingle('classpath:features/common/auth.feature', {
            username: user.username,
            password: user.password
        });

        config.authTokens[user.username] = result.authCookie;
    });

    return config;
}