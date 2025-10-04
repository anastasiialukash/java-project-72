@import gg.jte.Content
@param String title
@param String flash = null

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
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
            @if(flash != null)
                <div class="rounded-0 m-0 alert alert-dismissible fade show alert-success" role="alert">
                    <p class="m-0">${flash}</p>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            @endif
            
            <div class="container-fluid bg-dark p-5">
                <div class="row">
                    <div class="col-md-10 col-lg-8 mx-auto text-white">
                        <h1 class="display-3 mb-0">Анализатор страниц</h1>
                        <p class="lead">Бесплатно проверяйте сайты на SEO пригодность</p>
                        <form action="/urls" method="post" class="rss-form text-body">
                            <div class="row">
                                <div class="col">
                                    <div class="form-floating">
                                        <input id="url-input" autofocus="" type="text" required="" name="url" aria-label="url" class="form-control w-100" placeholder="ссылка" autocomplete="off">
                                        <label for="url-input">Ссылка</label>
                                    </div>
                                </div>
                                <div class="col-auto">
                                    <button type="submit" class="h-100 btn btn-lg btn-primary px-sm-5">Проверить</button>
                                </div>
                            </div>
                        </form>
                        <p class="mt-2 mb-0 text-muted">Пример: https://www.example.com</p>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <footer class="footer border-top py-3 mt-5 bg-light">
        <div class="container-xl">
            <div class="text-center">
            </div>
        </div>
    </footer>
</body>
</html>