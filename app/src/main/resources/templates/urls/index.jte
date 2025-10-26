@import hexlet.code.model.Url
@import java.util.List
@import java.time.format.DateTimeFormatter
@import java.time.ZoneId

@param List<Url> urls
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
            
            <h1>Сайты</h1>

            <table class="table table-bordered table-hover mt-3">
                <thead>
                <tr>
                    <th class="col-1">ID</th>
                    <th>Имя</th>
                    <th class="col-2">Последняя проверка</th>
                    <th class="col-1">Код ответа</th>
                </tr>
                </thead>
                <tbody>
                    @for(var url : urls)
                        <tr>
                            <td>${url.getId()}</td>
                            <td>
                                <a href="/urls/${url.getId()}">${url.getName()}</a>
                            </td>
                            <td>
                                @if(url.hasBeenChecked())
                                    ${DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(url.getLastCheckAt())}
                                @endif
                            </td>
                            <td>
                                @if(url.hasBeenChecked())
                                    ${url.getStatusCode()}
                                @endif
                            </td>
                        </tr>
                    @endfor
                </tbody>
            </table>
        </div>
    
            </section>
        </main>
        
    

</body></html>