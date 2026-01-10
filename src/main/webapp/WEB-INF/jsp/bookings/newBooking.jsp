<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouvelle Réservation - GCA Airlines</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<%@ include file="../navbar.jsp" %>

<div class="container mt-4">
    <h2 class="mb-4">🎫 Nouvelle Réservation</h2>

    <% if (request.getAttribute("errorMessage") != null) { %>
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <%= request.getAttribute("errorMessage") %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <div class="row">
        <div class="col-md-6">
            <div class="card mb-4">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0">🔍 Rechercher un passager existant</h5>
                </div>
                <div class="card-body">
                    <form method="post" action="bookings">
                        <input type="hidden" name="action" value="searchPassenger">
                        <div class="mb-3">
                            <label for="passportNumber" class="form-label">Numéro de Passeport *</label>
                            <input type="text" class="form-control" id="passportNumber"
                                   name="passportNumber" placeholder="Ex: ABC123456" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">
                            🔍 Rechercher
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-md-6">
            <div class="card mb-4">
                <div class="card-header bg-success text-white">
                    <h5 class="mb-0">➕ Créer un nouveau passager</h5>
                </div>
                <div class="card-body">
                    <form method="post" action="bookings">
                        <input type="hidden" name="action" value="createPassenger">

                        <div class="mb-3">
                            <label for="firstName" class="form-label">Prénom *</label>
                            <input type="text" class="form-control" id="firstName"
                                   name="firstName" placeholder="Jean" required>
                        </div>

                        <div class="mb-3">
                            <label for="lastName" class="form-label">Nom *</label>
                            <input type="text" class="form-control" id="lastName"
                                   name="lastName" placeholder="Dupont" required>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="jean.dupont@example.com">
                        </div>

                        <div class="mb-3">
                            <label for="phone" class="form-label">Téléphone</label>
                            <input type="tel" class="form-control" id="phone" name="phone"
                                   placeholder="+261 XX XXX XXXX">
                        </div>

                        <div class="mb-3">
                            <label for="passportNumberNew" class="form-label">Numéro de Passeport *</label>
                            <input type="text" class="form-control" id="passportNumberNew"
                                   name="passportNumber" placeholder="ABC123456" required>
                        </div>

                        <div class="mb-3">
                            <label for="dateOfBirth" class="form-label">Date de Naissance *</label>
                            <input type="date" class="form-control" id="dateOfBirth"
                                   name="dateOfBirth" required>
                        </div>

                        <button type="submit" class="btn btn-success w-100">
                            ➕ Créer et Continuer
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="mt-3">
        <a href="bookings" class="btn btn-secondary">↩️ Retour</a>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>