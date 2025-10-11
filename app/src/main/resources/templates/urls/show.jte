@import hexlet.code.model.Url
@import hexlet.code.model.UrlCheck
@import java.time.format.DateTimeFormatter
@import java.time.ZoneId
@import java.util.List

@param Url url
@param List<UrlCheck> checks
@param String flash = null

<!doctype html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Анализатор страниц</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4" crossorigin="anonymous"></script>
</head>

<body class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="/">Анализатор страниц</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <div class="navbar-nav">
                    <a class="nav-link" href="/">Главная</a>
                    <a class="nav-link" href="/urls">Сайты</a>
                </div>
            </div>
        </div>
    </nav>

    <main class="flex-grow-1">
            
            <section>
                
        <div class="container-lg mt-5">
            @if(flash != null)
                <div class="rounded-0 m-0 alert alert-dismissible fade show alert-success" role="alert">
                    <p class="m-0">${flash}</p>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            @endif
            
            <h1>Сайт: ${url.getName()}</h1>

            <table class="table table-bordered table-hover mt-3">
                <tbody>
                <tr>
                    <td>ID</td>
                    <td>${url.getId()}</td>
                </tr>
                <tr>
                    <td>Имя</td>
                    <td>${url.getName()}</td>
                </tr>
                <tr>
                    <td>Дата создания</td>
                    <td>${DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(url.getCreatedAt())}</td>
                </tr>
                </tbody>
            </table>

            <h2 class="mt-5">Проверки</h2>
            <form method="post" action="/urls/${url.getId()}/checks">
                <button type="submit" class="btn btn-primary">Запустить проверку</button>
            </form>

            <table class="table table-bordered table-hover mt-3">
                <thead>
                    <tr><th class="col-1">ID</th>
                    <th class="col-1">Код ответа</th>
                    <th>title</th>
                    <th>h1</th>
                    <th>description</th>
                    <th class="col-2">Дата проверки</th>
                </tr></thead>
                <tbody>
                    @for(var check : checks)
                        <tr>
                            <td>${check.getId()}</td>
                            <td>${check.getStatusCode()}</td>
                            <td>${check.getTitle()}</td>
                            <td>${check.getH1()}</td>
                            <td>${check.getDescription()}</td>
                            <td>${DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(check.getCreatedAt())}</td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        </div>
    
            </section>
        </main>

    <footer class="footer border-top py-3 mt-5 bg-light">
        <div class="container-xl">
            <div class="text-center">
                created by
                <a href="https://ru.hexlet.io" target="_blank">Hexlet</a>
            </div>
        </div>
    </footer>
    

</body></html>