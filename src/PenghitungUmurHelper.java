import java.time.LocalDate;
import java.time.Period;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;
import javax.swing.JTextArea;
import org.json.JSONArray;
import org.json.JSONObject;


public class PenghitungUmurHelper {

    public String hitungUmurDetail(LocalDate lahir, LocalDate sekarang) {
        Period period = Period.between(lahir, sekarang);
        return period.getYears() + " tahun, " + period.getMonths() + " bulan, " + period.getDays() + " hari";
    }

    public LocalDate hariUlangTahunBerikutnya(LocalDate lahir, LocalDate sekarang) {
        LocalDate ulangTahun = lahir.withYear(sekarang.getYear());
        if (!ulangTahun.isAfter(sekarang)) {
            ulangTahun = ulangTahun.plusYears(1);
        }
        return ulangTahun;
    }

    public String getDayOfWeekInIndonesian(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "Senin";
            case TUESDAY: return "Selasa";
            case WEDNESDAY: return "Rabu";
            case THURSDAY: return "Kamis";
            case FRIDAY: return "Jumat";
            case SATURDAY: return "Sabtu";
            case SUNDAY: return "Minggu";
            default: return "";
        }
    }
// Mendapatkan peristiwa penting secara baris per baris
public void getPeristiwaBarisPerBaris(LocalDate tanggal, JTextArea txtAreaPeristiwa, Supplier<Boolean> shouldStop) {
    try {
        // Periksa jika thread seharusnya dihentikan sebelum dimulai
        if (shouldStop.get()) {
            return;
        }

        // Buat URL API berdasarkan tanggal yang dipilih
        String urlString = "https://byabbe.se/on-this-day/" +
                tanggal.getMonthValue() + "/" + tanggal.getDayOfMonth() + "/events.json";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Periksa status HTTP response
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP response code: " + responseCode +
                    ". Silakan coba lagi nanti atau cek koneksi internet.");
        }

        // Membaca respon dari API
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            // Periksa jika thread seharusnya dihentikan saat membaca data
            if (shouldStop.get()) {
                in.close();
                conn.disconnect();
                javax.swing.SwingUtilities.invokeLater(() ->
                        txtAreaPeristiwa.setText("Pengambilan data dibatalkan.\n"));
                return;
            }
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        // Parsing JSON hasil dari API
        JSONObject json = new JSONObject(content.toString());
        JSONArray events = json.getJSONArray("events");

        for (int i = 0; i < events.length(); i++) {
            // Periksa jika thread seharusnya dihentikan sebelum memproses data
            if (shouldStop.get()) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        txtAreaPeristiwa.setText("Pengambilan data dibatalkan.\n"));
                return;
            }

            JSONObject event = events.getJSONObject(i);
            String year = event.getString("year");
            String description = event.getString("description");
            String peristiwa = year + ": " + description;

            javax.swing.SwingUtilities.invokeLater(() ->
                    txtAreaPeristiwa.append(peristiwa + "\n"));
        }

        // Jika tidak ada peristiwa ditemukan
        if (events.length() == 0) {
            javax.swing.SwingUtilities.invokeLater(() ->
                    txtAreaPeristiwa.setText("Tidak ada peristiwa penting yang ditemukan pada tanggal ini."));
        }

    } catch (Exception e) {
        javax.swing.SwingUtilities.invokeLater(() ->
                txtAreaPeristiwa.setText("Gagal mendapatkan data peristiwa: " + e.getMessage()));
    }
}
}

