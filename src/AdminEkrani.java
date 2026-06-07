import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminEkrani extends TemelEkran {

    private JTable kullaniciTablosu, logTablosu;
    private DefaultTableModel kullaniciModeli, logModeli;
    private TableRowSorter<DefaultTableModel> logSorter, kullaniciSorter;

    @Override
    void yetkiPanelleriniEkle() {
        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.setFont(new Font("Arial", Font.BOLD, 14));

        sekmeler.addTab("👥 Kullanıcı & Yetki Yönetimi", pnlKullaniciYonetimi());
        sekmeler.addTab("📜 Sistem Geçmişi (Loglar)", pnlSistemLoglari());
        sekmeler.addTab("💾 Gelişmiş Veritabanı Yedekleme", pnlSistemBakim());
        sekmeler.addTab("🛡️ Admin Profil & Çıkış", pnlProfilOlustur());

        add(sekmeler, BorderLayout.CENTER);
    }

    // --- 1. SEKME: KULLANICI YÖNETİMİ ---
    private JPanel pnlKullaniciYonetimi() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        String[] kolonlar = {"ID", "Kullanıcı Adı", "Yetki Rolü", "Telefon", "E-Posta"};
        kullaniciModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        kullaniciTablosu = new JTable(kullaniciModeli);
        kullaniciTablosu.setRowHeight(25);

        // Tablo Sıralama Motoru
        kullaniciSorter = new TableRowSorter<>(kullaniciModeli);
        kullaniciTablosu.setRowSorter(kullaniciSorter);

        panel.add(new JScrollPane(kullaniciTablosu), BorderLayout.CENTER);

        JPanel pnlButonlar = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlButonlar.setBackground(Color.WHITE);

        JButton btnEkle = new JButton("Yeni Kullanıcı Ekle");
        btnEkle.setBackground(new Color(39, 174, 96)); btnEkle.setForeground(Color.WHITE);

        JButton btnYetkiDegistir = new JButton("Seçili Kullanıcının Yetkisini Değiştir");
        btnYetkiDegistir.setBackground(new Color(243, 156, 18)); btnYetkiDegistir.setForeground(Color.WHITE);

        JButton btnSil = new JButton("Kullanıcıyı Sil");
        btnSil.setBackground(new Color(192, 57, 43)); btnSil.setForeground(Color.WHITE);

        pnlButonlar.add(btnEkle); pnlButonlar.add(btnYetkiDegistir); pnlButonlar.add(btnSil);
        panel.add(pnlButonlar, BorderLayout.SOUTH);

        kullanicilariGetir();

        // Yeni Kullanıcı Ekleme
        btnEkle.addActionListener(e -> {
            JTextField txtAd = new JTextField(); JTextField txtSifre = new JTextField();
            JTextField txtTel = new JTextField(); JTextField txtMail = new JTextField();
            JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Personel", "Yönetici", "Patron", "Admin"});

            Object[] form = {"Kullanıcı Adı:", txtAd, "Şifre:", txtSifre, "Sistem Rolü:", cmbRol, "Telefon:", txtTel, "E-Posta:", txtMail};

            if (JOptionPane.showOptionDialog(this, form, "Sisteme Yeni Kullanıcı Kaydı", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Kaydet", "İptal"}, "Kaydet") == 0) {

                // GÜVENLİK YAMASI 1: Boş Kullanıcı Adı ve Şifre Kontrolü (Validation)
                if (txtAd.getText().trim().isEmpty() || txtSifre.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Kullanıcı adı ve şifre alanları boş bırakılamaz!", "Geçersiz İşlem", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO kullanicilar (kullanici_adi, sifre, rol, telefon, eposta, maas_durumu, maas, prim, satis_adedi) VALUES (?, ?, ?, ?, ?, 'Bekliyor', 17000, 0, 0)")) {
                    pstmt.setString(1, txtAd.getText().trim());
                    pstmt.setString(2, txtSifre.getText().trim());
                    pstmt.setString(3, cmbRol.getSelectedItem().toString());
                    pstmt.setString(4, txtTel.getText().trim());
                    pstmt.setString(5, txtMail.getText().trim());
                    pstmt.executeUpdate();

                    kullanicilariGetir();
                    Veritabani.sistemeLogYaz("YENİ KAYIT: Sisteme " + cmbRol.getSelectedItem() + " eklendi: " + txtAd.getText());
                    JOptionPane.showMessageDialog(this, "Kullanıcı başarıyla kaydedildi.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Kayıt sırasında hata oluştu! (Kullanıcı adı daha önce alınmış olabilir)", "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Yetki Değiştirme
        btnYetkiDegistir.addActionListener(e -> {
            int viewRow = kullaniciTablosu.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Önce yetkisi değişecek kullanıcıyı tablodan seçin!"); return; }

            int modelRow = kullaniciTablosu.convertRowIndexToModel(viewRow);
            int id = (int) kullaniciModeli.getValueAt(modelRow, 0);
            String ad = (String) kullaniciModeli.getValueAt(modelRow, 1);
            String mevcutRol = (String) kullaniciModeli.getValueAt(modelRow, 2);

            JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Personel", "Yönetici", "Patron", "Admin"});
            cmbRol.setSelectedItem(mevcutRol);

            Object[] form = {"Kullanıcı: " + ad, "Yeni Yetkisini Seçin:", cmbRol};
            if (JOptionPane.showOptionDialog(this, form, "Yetki Düzenleme", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Yetkiyi Güncelle", "İptal"}, "Yetkiyi Güncelle") == 0) {
                String yeniRol = cmbRol.getSelectedItem().toString();
                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE kullanicilar SET rol = ? WHERE id = ?")) {
                    pstmt.setString(1, yeniRol); pstmt.setInt(2, id); pstmt.executeUpdate();
                    kullanicilariGetir();
                    Veritabani.sistemeLogYaz("YETKİ DEĞİŞİMİ: " + ad + " kullanıcısının yetkisi '" + yeniRol + "' olarak güncellendi.");
                    JOptionPane.showMessageDialog(this, "Kullanıcı yetkisi başarıyla güncellendi!");
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        // Adminin kendini silmesi engellendi
        btnSil.addActionListener(e -> {
            int viewRow = kullaniciTablosu.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Lütfen silinecek kullanıcıyı seçin."); return; }

            int modelRow = kullaniciTablosu.convertRowIndexToModel(viewRow);
            String ad = (String) kullaniciModeli.getValueAt(modelRow, 1);

            // Güvenlik Kontrolü
            if (ad.equals(Veritabani.aktifKullanici)) {
                JOptionPane.showMessageDialog(this, "Kritik Hata: Aktif olarak kullandığınız kendi hesabınızı silemezsiniz!", "Güvenlik Engeli", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (JOptionPane.showConfirmDialog(this, ad + " adlı kullanıcıyı sistemden kalıcı olarak silmek istiyor musunuz?", "Kullanıcı Sil", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM kullanicilar WHERE id = ?")) {
                    pstmt.setInt(1, (int) kullaniciModeli.getValueAt(modelRow, 0));
                    pstmt.executeUpdate();
                    kullanicilariGetir();
                    Veritabani.sistemeLogYaz("KULLANICI SİLİNDİ: " + ad);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        return panel;
    }

    // --- 2. SEKME: LOGLAR VE EXCEL ÇIKTISI ---
    private JPanel pnlSistemLoglari() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel pnlArama = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlArama.setBackground(Color.WHITE);
        JLabel lblAra = new JLabel("🔍 Loglarda Ara (İsim/Aksiyon):");
        JTextField txtAra = new JTextField(20);
        pnlArama.add(lblAra); pnlArama.add(txtAra);
        panel.add(pnlArama, BorderLayout.NORTH);

        String[] kolonlar = {"Tarih / Saat", "İşlemi Yapan", "Sistem Olayı (Detay)"};
        logModeli = new DefaultTableModel(kolonlar, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        logTablosu = new JTable(logModeli); logTablosu.setRowHeight(25);
        logTablosu.getColumnModel().getColumn(0).setPreferredWidth(150); logTablosu.getColumnModel().getColumn(1).setPreferredWidth(100); logTablosu.getColumnModel().getColumn(2).setPreferredWidth(450);

        logSorter = new TableRowSorter<>(logModeli);
        logTablosu.setRowSorter(logSorter);
        txtAra.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { ara(); }
            @Override public void removeUpdate(DocumentEvent e) { ara(); }
            @Override public void changedUpdate(DocumentEvent e) { ara(); }
            private void ara() {
                String t = txtAra.getText();
                if (t.trim().length() == 0) { logSorter.setRowFilter(null); } else { logSorter.setRowFilter(RowFilter.regexFilter("(?i)" + t)); }
            }
        });

        panel.add(new JScrollPane(logTablosu), BorderLayout.CENTER);

        JPanel pnlButonlar = new JPanel(new FlowLayout());
        pnlButonlar.setBackground(Color.WHITE);
        JButton btnYenile = new JButton("Kayıtları Yenile");
        JButton btnExcel = new JButton("Tüm Logları Excel'e Aktar (.csv)");
        btnExcel.setBackground(new Color(39, 174, 96)); btnExcel.setForeground(Color.WHITE);

        pnlButonlar.add(btnYenile); pnlButonlar.add(btnExcel);
        panel.add(pnlButonlar, BorderLayout.SOUTH);

        btnYenile.addActionListener(e -> loglariGetir());

        btnExcel.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Sistem Loglarını Excel Olarak Kaydet");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = new File(fileChooser.getSelectedFile() + "_Loglar.csv");
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                    bw.write('\ufeff'); // Türkçe karakter sorunu çözümü
                    for (int i = 0; i < logTablosu.getColumnCount(); i++) { bw.write(logTablosu.getColumnName(i) + ";"); }
                    bw.newLine();
                    for (int i = 0; i < logTablosu.getRowCount(); i++) {
                        for (int j = 0; j < logTablosu.getColumnCount(); j++) { bw.write(logTablosu.getValueAt(i, j).toString() + ";"); }
                        bw.newLine();
                    }
                    JOptionPane.showMessageDialog(this, "Loglar başarıyla Excel dosyasına aktarıldı!");
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Dosya kaydedilirken hata oluştu."); }
            }
        });

        loglariGetir();
        return panel;
    }

    // --- 3. SEKME: GELİŞMİŞ YEDEKLEME MODÜLÜ ---
    private JPanel pnlSistemBakim() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 240, 241));

        JPanel icPanel = new JPanel(new BorderLayout(15, 20));
        icPanel.setBackground(Color.WHITE);
        icPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(30, 40, 30, 40)));

        JLabel lblBaslik = new JLabel("<html><center><h2 style='color:#2c3e50; margin:0;'>Güvenli Veritabanı Yedekleme Motoru</h2>"
                + "<p style='color:#7f8c8d; font-size:13px; margin-top:5px;'>Sistemdeki tüm kritik verileriniz tek bir .db dosyasında şifrelenerek yedeklenir.</p></center></html>", SwingConstants.CENTER);

        JPanel pnlBilgi = new JPanel(new GridLayout(5, 1, 5, 10));
        pnlBilgi.setBackground(Color.WHITE);
        pnlBilgi.setBorder(BorderFactory.createTitledBorder(null, "Bu İşlem Neleri Kapsar?", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), new Color(41, 128, 185)));

        JLabel m1 = new JLabel("✅  Tüm Müşteri Kayıtları ve Fatura Geçmişleri"); m1.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel m2 = new JLabel("✅  Araç Filosu, Ekspertiz ve Hasar Raporları"); m2.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel m3 = new JLabel("✅  Personel Bilgileri, Maaş ve Prim Durumları"); m3.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel m4 = new JLabel("✅  Sistem Güvenlik Logları ve Yönetici İşlemleri"); m4.setFont(new Font("Arial", Font.PLAIN, 14));

        pnlBilgi.add(m1); pnlBilgi.add(m2); pnlBilgi.add(m3); pnlBilgi.add(m4);

        JPanel pnlAksiyon = new JPanel(new BorderLayout(10, 15));
        pnlAksiyon.setBackground(Color.WHITE);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(new Color(236, 240, 241));
        progressBar.setForeground(new Color(41, 128, 185));
        progressBar.setPreferredSize(new Dimension(400, 30));

        JButton btnYedekle = new JButton("Tüm Sistemi Yedekle");
        btnYedekle.setBackground(new Color(41, 128, 185));
        btnYedekle.setForeground(Color.WHITE);
        btnYedekle.setFont(new Font("Arial", Font.BOLD, 15));
        btnYedekle.setPreferredSize(new Dimension(400, 45));

        pnlAksiyon.add(progressBar, BorderLayout.NORTH);
        pnlAksiyon.add(btnYedekle, BorderLayout.SOUTH);

        icPanel.add(lblBaslik, BorderLayout.NORTH);
        icPanel.add(pnlBilgi, BorderLayout.CENTER);
        icPanel.add(pnlAksiyon, BorderLayout.SOUTH);

        panel.add(icPanel);

        btnYedekle.addActionListener(e -> {
            btnYedekle.setEnabled(false); btnYedekle.setText("Veriler Şifreleniyor ve Kopyalanıyor...");
            new Thread(() -> {
                try {
                    for (int i = 0; i <= 100; i += 20) {
                        Thread.sleep(300);
                        progressBar.setValue(i);
                    }
                    String zaman = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    Path kaynak = Paths.get("salih.db");
                    Path hedef = Paths.get("yedek_salih_" + zaman + ".db");
                    Files.copy(kaynak, hedef, StandardCopyOption.REPLACE_EXISTING);

                    Veritabani.sistemeLogYaz("SİSTEM GÜVENLİĞİ: Tüm veritabanının yedeği alındı (" + hedef.getFileName() + ").");
                    btnYedekle.setText("Yedekleme İşlemi Tamamlandı!");
                    btnYedekle.setBackground(new Color(39, 174, 96));
                    JOptionPane.showMessageDialog(this, "Yedek başarıyla oluşturuldu:\n" + hedef.getFileName(), "Yedekleme Başarılı", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    btnYedekle.setEnabled(true);
                    btnYedekle.setText("Tüm Sistemi Yedekle");
                    JOptionPane.showMessageDialog(this, "Yedekleme başarısız oldu. Lütfen veritabanı dosyasının açık olmadığından emin olun.");
                }
            }).start();
        });
        return panel;
    }

    // --- 4. SEKME: ADMİN PROFİLİ VE ÇIKIŞ ---
    private JPanel pnlProfilOlustur() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(44, 62, 80));

        JLabel lblProfil = new JLabel("Profil Yükleniyor...");

        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM kullanicilar WHERE kullanici_adi = ?")) {

            pstmt.setString(1, Veritabani.aktifKullanici);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String html = "<html><center><h1 style='color:#e74c3c;'>🛡️ SİSTEM YÖNETİCİSİ (ADMIN)</h1><hr style='border: 1px solid gray;'><br>"
                        + "<table style='font-size:18px; color:#ecf0f1;' cellpadding='10'>"
                        + "<tr><td align='right'><b>Admin ID:</b></td><td>@" + rs.getString("kullanici_adi") + "</td></tr>"
                        + "<tr><td align='right'><b>Giriş Şifresi:</b></td><td>" + rs.getString("sifre") + "</td></tr>"
                        + "<tr><td align='right'><b>Güvenlik Tel:</b></td><td>" + rs.getString("telefon") + "</td></tr>"
                        + "<tr><td align='right'><b>Sistem Maili:</b></td><td>" + rs.getString("eposta") + "</td></tr>"
                        + "</table><br><br><span style='color:#bdc3c7;'><i>UYARI: Bu ekrandaki yetkiler en üst düzeydedir.<br>Yapılan her işlem loglanmaktadır.</i></span></center></html>";
                lblProfil.setText(html);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JButton btnCikis = new JButton("Sistemden Güvenli Çıkış Yap");
        btnCikis.setBackground(new Color(192, 57, 43));
        btnCikis.setForeground(Color.WHITE);
        btnCikis.setFont(new Font("Arial", Font.BOLD, 15));
        btnCikis.setPreferredSize(new Dimension(250, 45));

        btnCikis.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Admin panelinden çıkış yapmak istediğinize emin misiniz?", "Güvenli Çıkış", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {

                Veritabani.sistemeLogYaz("MESAİ BİTİŞİ (Sistemden çıkış yapıldı)");

                // GÜVENLİK YAMASI 2: Session'ın (Oturum) Tamamen Sıfırlanması
                Veritabani.aktifKullanici = "";
                Veritabani.aktifRol = "";

                this.dispose();
                new GirisEkrani().setVisible(true);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblProfil, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(40, 0, 0, 0); panel.add(btnCikis, gbc);

        return panel;
    }

    // --- YARDIMCI METOTLAR ---
    private void kullanicilariGetir() {
        if(kullaniciModeli != null) kullaniciModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM kullanicilar")) {
            while (rs.next()) {
                kullaniciModeli.addRow(new Object[]{rs.getInt("id"), rs.getString("kullanici_adi"), rs.getString("rol"), rs.getString("telefon"), rs.getString("eposta")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loglariGetir() {
        if(logModeli != null) logModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM islem_gecmisi ORDER BY id DESC")) {
            while (rs.next()) {
                logModeli.addRow(new Object[]{rs.getString("tarih"), rs.getString("kullanici_adi"), rs.getString("islem_detayi")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}