@import hexlet.model.Url
@import java.util.List
@import java.text.SimpleDateFormat

@param List<Url> urls
@param String flash = null

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>URLs List</title>
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
                        <a class="nav-link active" href="/urls">URLs</a>
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

            <h1 class="mb-4">URLs</h1>

            <table class="table table-bordered table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>URL</th>
                        <th>Created At</th>
                    </tr>
                </thead>
                <tbody>
                    @for(var url : urls)
                        <tr>
                            <td>${url.getId()}</td>
                            <td>
                                <a href="/urls/${url.getId()}">${url.getName()}</a>
                            </td>
                            <td>${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(url.getCreatedAt())}</td>
                        </tr>
                    @endfor
                </tbody>
            </table>
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