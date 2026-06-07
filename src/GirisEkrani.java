import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class GirisEkrani extends JFrame {
    public GirisEkrani() {
        setTitle("Anadolu Lojistik - Kurumsal Giriş");
        setSize(450, 350); // Ekran boyutu ferahlatıldı
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // Tasarımın bozulmaması için yeniden boyutlandırma kapatıldı
        setLayout(new BorderLayout());

        // --- 1. ÜST PANEL: KURUMSAL BAŞLIK ---
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1));
        pnlHeader.setBackground(new Color(44, 62, 80)); // Koyu Lacivert (Modern Kurumsal Renk)
        pnlHeader.setBorder(new EmptyBorder(20, 0, 20, 0)); // Üstten ve alttan boşluk

        JLabel lblTitle = new JLabel("ANADOLU LOJİSTİK", SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel lblSub = new JLabel("Araç Kiralama ve Filo Yönetim Sistemi", SwingConstants.CENTER);
        lblSub.setForeground(new Color(189, 195, 199)); // Açık Gri
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. ORTA PANEL: KULLANICI GİRİŞ FORMU ---
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Elementler arası boşluk
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Kullanıcı Adı:");
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setForeground(new Color(52, 73, 94));

        JTextField txtKullanici = new JTextField(15);
        txtKullanici.setFont(new Font("Arial", Font.PLAIN, 15));
        txtKullanici.setPreferredSize(new Dimension(200, 35)); // Daha geniş metin kutusu

        JLabel lblPass = new JLabel("Sistem Şifresi:");
        lblPass.setFont(new Font("Arial", Font.BOLD, 14));
        lblPass.setForeground(new Color(52, 73, 94));

        JPasswordField txtSifre = new JPasswordField(15);
        txtSifre.setFont(new Font("Arial", Font.PLAIN, 15));
        txtSifre.setPreferredSize(new Dimension(200, 35));

        // GridBagLayout Yerleşimi
        gbc.gridx = 0; gbc.gridy = 0; pnlCenter.add(lblUser, gbc);
        gbc.gridx = 1; gbc.gridy = 0; pnlCenter.add(txtKullanici, gbc);
        gbc.gridx = 0; gbc.gridy = 1; pnlCenter.add(lblPass, gbc);
        gbc.gridx = 1; gbc.gridy = 1; pnlCenter.add(txtSifre, gbc);

        add(pnlCenter, BorderLayout.CENTER);

        // --- 3. ALT PANEL: AKSİYON BUTONU ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(0, 0, 25, 0));

        JButton btnGiris = new JButton("Sisteme Giriş Yap");
        btnGiris.setFont(new Font("Arial", Font.BOLD, 16));
        btnGiris.setBackground(new Color(41, 128, 185)); // Mavi Buton
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setFocusPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGiris.setPreferredSize(new Dimension(320, 45)); // Buton genişletildi

        pnlBottom.add(btnGiris);
        add(pnlBottom, BorderLayout.SOUTH);

        // --- FONKSİYONELLİK VE OLAY (EVENT) DİNLEYİCİLERİ ---

        // Enter tuşu kısayolları
        txtKullanici.addActionListener(e -> txtSifre.requestFocus());
        txtSifre.addActionListener(e -> btnGiris.doClick());

        btnGiris.addActionListener(e -> {
            String kAd = txtKullanici.getText().trim();
            String kSifre = new String(txtSifre.getPassword()).trim();

            if(kAd.isEmpty() || kSifre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen kullanıcı adı ve şifre alanlarını boş bırakmayınız!", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // GİRİŞ EKRANININ DONMASINI ENGELLEYEN PROFESYONEL YAPI
            btnGiris.setEnabled(false); // Butonu geçici kilitle
            btnGiris.setText("Bağlantı Kuruluyor, Bekleyin..."); // Yükleniyor animasyonu
            btnGiris.setBackground(new Color(149, 165, 166)); // Pasif renk

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                String rol = "";

                @Override
                protected Boolean doInBackground() throws Exception {
                    // Veritabanı işlemi arkaplanda yapılır, UI donmaz
                    try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                         PreparedStatement pstmt = conn.prepareStatement("SELECT rol FROM kullanicilar WHERE kullanici_adi = ? AND sifre = ?")) {

                        pstmt.setString(1, kAd);
                        pstmt.setString(2, kSifre);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            rol = rs.getString("rol");
                            return true; // Giriş başarılı
                        }
                    }
                    return false; // Hatalı şifre
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            // Değişkenleri güvenli alanda (Bağlantı kapandıktan sonra) atıyoruz
                            Veritabani.aktifKullanici = kAd;
                            Veritabani.aktifRol = rol;

                            Veritabani.sistemeLogYaz("MESAİ BAŞLANGICI (Sisteme giriş yapıldı)");

                            dispose(); // Mevcut ekranı kapat

                            // Yeni ekranı aç
                            switch (Veritabani.aktifRol) {
                                case "Personel": new PersonelEkrani().setVisible(true); break;
                                case "Yönetici": new YoneticiEkrani().setVisible(true); break;
                                case "Patron":   new PatronEkrani().setVisible(true); break;
                                case "Admin":    new AdminEkrani().setVisible(true); break;
                            }
                        } else {
                            // Hatalı şifre durumunda butonu geri aç
                            btnGiris.setEnabled(true);
                            btnGiris.setText("Sisteme Giriş Yap");
                            btnGiris.setBackground(new Color(41, 128, 185));
                            JOptionPane.showMessageDialog(GirisEkrani.this, "Hatalı kullanıcı adı veya şifre!", "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        btnGiris.setEnabled(true);
                        btnGiris.setText("Sisteme Giriş Yap");
                        btnGiris.setBackground(new Color(41, 128, 185));
                        JOptionPane.showMessageDialog(GirisEkrani.this, "Veritabanı bağlantı hatası! Lütfen sistemi kontrol edin.", "Kritik Hata", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute(); // Arkaplan işlemini başlat
        });
    }
}