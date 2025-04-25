package tn.esprit.models;


import java.time.LocalDate;

    public class Reponse {
        private int id;
        private String contenu;
        private LocalDate dateReponse;
        private int reclamationId;

        public Reponse() {}

        public Reponse(String contenu, LocalDate dateReponse, int reclamationId) {
            this.contenu = contenu;
            this.dateReponse = dateReponse;
            this.reclamationId = reclamationId;
        }

        public Reponse(int id, String contenu, LocalDate dateReponse, int reclamationId) {
            this.id = id;
            this.contenu = contenu;
            this.dateReponse = dateReponse;
            this.reclamationId = reclamationId;
        }

        public int getId() { return id; }
        public String getContenu() { return contenu; }
        public LocalDate getDateReponse() { return dateReponse; }
        public int getReclamationId() { return reclamationId; }

        public void setId(int id) { this.id = id; }
        public void setContenu(String contenu) { this.contenu = contenu; }
        public void setDateReponse(LocalDate dateReponse) { this.dateReponse = dateReponse; }
        public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

        @Override
        public String toString() {
            return "Reponse{" +
                    "id=" + id +
                    ", contenu='" + contenu + '\'' +
                    ", dateReponse=" + dateReponse +
                    ", reclamationId=" + reclamationId +
                    '}';
        }
    }


