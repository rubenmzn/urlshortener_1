document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('shortener').addEventListener('submit', function (event) {
        event.preventDefault();
        var generateQr = document.getElementById('qr').checked;

        fetch('/api/link', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams(new FormData(this)).toString() + '&qr=' + generateQr,
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error en la solicitud');
            }
            return response.json();
        })
        .then(msg => {
            console.log(msg);
            if (msg.properties.qr !== null) {
                document.getElementById('result').innerHTML =
                    "<div class='alert alert-success lead'><a target='_blank' href='"
                    + msg.url
                    + "'>"
                    + msg.url
                    + "</a></div>"
                    //+ "<div><img src='" + msg.properties.qr + "' alt='QR Code' width='200' height='200'/></div>";
                    + "<div><a>" +  msg.properties.qr + "</a></div>";

                // Muestra la etiqueta img con el código QR
                //document.getElementById('qrimg').src = msg.properties.qr;
                //document.getElementById('qrimg').style.display = 'block';
            } else {
                document.getElementById('result').innerHTML =
                    "<div class='alert alert-success lead'><a target='_blank' href='"
                    + msg.url
                    + "'>"
                    + msg.url
                    + "</a></div>";
            }
        })
        .catch(error => {
            document.getElementById('result').innerHTML =
                "<div class='alert alert-danger lead'>ERROR</div>";
        });
    });

    // Manejar el formulario para procesar archivos CSV
    document.getElementById('csvForm').addEventListener('submit', function (event) {
        event.preventDefault();

        var fileInput = document.getElementById('csvFile');
        var file = fileInput.files[0];

        var formData = new FormData(this);

        fetch('/api/bulk', {
            method: 'POST',
            body: formData,
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error en la solicitud del archivo CSV');
            }
            return response.text();
        })
        .then(csvContent => {
            // Manejar la respuesta del servidor para archivos CSV según sea necesario
            console.log(csvContent);

            // Configurar el encabezado de la respuesta para indicar la descarga del archivo
            var blob = new Blob([csvContent], { type: 'text/csv' });
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = 'urls.csv';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        })
        .catch(error => {
            // Manejar errores en la petición para archivos CSV
            console.log('Error al manejar el archivo CSV');
        });
    });
});
