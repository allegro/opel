<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Opel demo</title>
    <link href="https://fonts.googleapis.com/css?family=Roboto:400,400i,700,700i" rel="stylesheet">
    <link type="text/css" rel="stylesheet" href="/normalize.css">
    <link type="text/css" rel="stylesheet" href="/skeleton.css">
    <style>
        textarea[name="expression"] {
            height: 12em;
            font-family: "Courier New", Courier, monospace;
        }

        .error {
            color: #ba271f;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Opel demo</h1>
        <form action="/" method="GET">
            <div class="row">
                <textarea name="expression" class="u-full-width">${expression!'2 + 2'}</textarea>
            </div>
            <div class="row">
                <input type="submit" value="compute!" class="button-primary"/>
            </div>
        </form>
        <div class="row">
            <#if error??>
                <pre><code class="error">${error}</code></pre>
            </#if>
            <#if result??>
                <pre><code>${result}</code></pre>
            </#if>
        </div>
    </div>
</body>
</html>
