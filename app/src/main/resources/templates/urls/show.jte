@import hexlet.model.Url
@import hexlet.model.UrlCheck
@import java.text.SimpleDateFormat
@import java.util.List

@param Url url
@param List<UrlCheck> checks
@param String flash = null

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>URL Details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">Site Analyzer</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <a class="nav-link" href="/">Home</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/urls">URLs</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <main class="flex-grow-1">
        <div class="container mt-4">
            @if(flash != null)
                <div class="alert alert-info" role="alert">
                    ${flash}
                </div>
            @endif

            <h1 class="mb-4">URL Details</h1>

            <table class="table table-bordered">
                <tbody>
                    <tr>
                        <th>ID</th>
                        <td>${url.getId()}</td>
                    </tr>
                    <tr>
                        <th>URL</th>
                        <td>${url.getName()}</td>
                    </tr>
                    <tr>
                        <th>Created At</th>
                        <td>${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(url.getCreatedAt())}</td>
                    </tr>
                </tbody>
            </table>

            <form action="/urls/${url.getId()}/checks" method="post">
                <button type="submit" class="btn btn-primary">Check</button>
            </form>

            <div class="mt-5">
                <h2>Checks</h2>
                <table class="table table-bordered table-hover mt-3">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Status Code</th>
                            <th>Title</th>
                            <th>H1</th>
                            <th>Description</th>
                            <th>Created At</th>
                        </tr>
                    </thead>
                    <tbody>
                        @for(var check : checks)
                            <tr>
                                <td>${check.getId()}</td>
                                <td>${check.getStatusCode()}</td>
                                <td>${check.getTitle()}</td>
                                <td>${check.getH1()}</td>
                                <td>${check.getDescription()}</td>
                                <td>${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(check.getCreatedAt())}</td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>

            <a href="/urls" class="btn btn-primary">Back to URLs</a>
        </div>
    </main>

    <footer class="footer border-top py-3 mt-5 bg-light">
        <div class="container">
            <div class="text-center">
                Created by Hexlet
            </div>
        </div>
    </footer>
</body>
</html>