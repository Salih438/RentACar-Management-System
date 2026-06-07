import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.text.SimpleDateFormat;
import java.util.Date;

public class PersonelEkrani extends TemelEkran {

    private JTable tablo, faturaTablosu;
    private DefaultTableModel tabloModeli, faturaModeli;
    private TableRowSorter<DefaultTableModel> rowSorter;

    @Override
    void yetkiPanelleriniEkle() {
        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.setFont(new Font("Arial", Font.BOLD, 14));

        sekmeler.addTab("🚗 Araç Kiralama & Sözleşme", pnlAracIslemleriOlustur());
        sekmeler.addTab("⏱️ Aktif Kiralamalar & Takip", pnlAktifKiralamalar()); // YENİ EKLENEN SEKME
        sekmeler.addTab("📜 Geçmiş Faturalar ve Satışlar", pnlFaturalar());
        sekmeler.addTab("👤 Profil & Güvenli Çıkış", pnlProfilOlustur());

        add(sekmeler, BorderLayout.CENTER);
    }

    // --- 1. SEKME: ARAÇ KİRALAMA VE EKSPERTİZ ---
    private JPanel pnlAracIslemleriOlustur() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel pnlArama = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlArama.setBackground(new Color(236, 240, 241));
        pnlArama.setBorder(BorderFactory.createTitledBorder("Araç Filtreleme"));
        JLabel lblAra = new JLabel("🔍 Araç Ara (Model/Durum): ");
        lblAra.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtAra = new JTextField(25);
        pnlArama.add(lblAra); pnlArama.add(txtAra);
        panel.add(pnlArama, BorderLayout.NORTH);

        String[] kolonlar = {"ID", "Model", "Fiyat", "Durum", "Temizlik", "Sigorta", "Ekspertiz (Hasar Raporu)"};
        tabloModeli = new DefaultTableModel(kolonlar, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablo = new JTable(tabloModeli);
        tablo.setRowHeight(25);

        rowSorter = new TableRowSorter<>(tabloModeli);
        tablo.setRowSorter(rowSorter);
        txtAra.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { ara(); }
            @Override public void removeUpdate(DocumentEvent e) { ara(); }
            @Override public void changedUpdate(DocumentEvent e) { ara(); }
            private void ara() {
                String text = txtAra.getText();
                if (text.trim().length() == 0) { rowSorter.setRowFilter(null); }
                else { rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); }
            }
        });

        panel.add(new JScrollPane(tablo), BorderLayout.CENTER);

        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        JButton btnKirala = new JButton("Sözleşme Oluştur ve Kirala");
        btnKirala.setBackground(new Color(39, 174, 96)); btnKirala.setForeground(Color.WHITE);
        JButton btnIade = new JButton("İade Al");
        btnIade.setBackground(new Color(41, 128, 185)); btnIade.setForeground(Color.WHITE);
        JButton btnEkspertiz = new JButton("🔍 Detaylı Ekspertiz Gör / Güncelle");
        btnEkspertiz.setBackground(new Color(243, 156, 18)); btnEkspertiz.setForeground(Color.WHITE);

        pnlButonlar.add(btnKirala); pnlButonlar.add(btnIade); pnlButonlar.add(btnEkspertiz);
        panel.add(pnlButonlar, BorderLayout.SOUTH);

        tabloyuYenile();

        btnKirala.addActionListener(e -> {
            int viewRow = tablo.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Önce tablodan araç seçin!"); return; }
            int modelRow = tablo.convertRowIndexToModel(viewRow);

            if (!tabloModeli.getValueAt(modelRow, 3).equals("Boşta")) { JOptionPane.showMessageDialog(this, "Araç boşta değil!"); return; }

            JTextField txtAd = new JTextField();
            JTextField txtTc = new JTextField();
            JTextField txtEhliyet = new JTextField();
            JTextField txtTel = new JTextField();
            JTextField txtSaat = new JTextField();

            Object[] form = {
                    "Müşteri Ad Soyad:", txtAd,
                    "TC Kimlik No:", txtTc,
                    "Ehliyet Belge No:", txtEhliyet,
                    "Telefon:", txtTel,
                    "Süre (Saat):", txtSaat
            };

            if (JOptionPane.showConfirmDialog(this, form, "Kiralama Kaydı", JOptionPane.OK_CANCEL_OPTION) == 0) {
                try {
                    int id = (int) tabloModeli.getValueAt(modelRow, 0);
                    String model = (String) tabloModeli.getValueAt(modelRow, 1);
                    int fiyat = Integer.parseInt(tabloModeli.getValueAt(modelRow, 2).toString().replace(" ₺", "").trim());
                    String hasar = (String) tabloModeli.getValueAt(modelRow, 6);
                    int sure = Integer.parseInt(txtSaat.getText());
                    int toplam = (int)((fiyat * sure) * 1.20);
                    String tarih = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

                    try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                         PreparedStatement pstmt = conn.prepareStatement("INSERT INTO satis_faturalari (musteri_adi, tc_no, ehliyet_no, telefon, arac_model, kiralama_suresi, toplam_tutar, hasar_durumu, islem_tarihi, personel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                        pstmt.setString(1, txtAd.getText());
                        pstmt.setString(2, txtTc.getText());
                        pstmt.setString(3, txtEhliyet.getText());
                        pstmt.setString(4, txtTel.getText());
                        pstmt.setString(5, model);
                        pstmt.setInt(6, sure);
                        pstmt.setInt(7, toplam);
                        pstmt.setString(8, hasar);
                        pstmt.setString(9, tarih);
                        pstmt.setString(10, Veritabani.aktifKullanici);

                        pstmt.executeUpdate();

                        try (PreparedStatement psSatis = conn.prepareStatement("UPDATE kullanicilar SET satis_adedi = satis_adedi + 1 WHERE kullanici_adi = ?")) {
                            psSatis.setString(1, Veritabani.aktifKullanici);
                            psSatis.executeUpdate();
                        }
                    }
                    aracSutunGuncelle(id, "\"Durum \"", "Kiralamada");
                    Veritabani.sistemeLogYaz("KİRALAMA: " + model + " (Müşteri: " + txtAd.getText() + " - TC: " + txtTc.getText() + ")");
                    faturalariCek();

                    JOptionPane.showMessageDialog(this, "Kiralama Başarılı! Sözleşme ve fatura oluşturuldu.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Hata: Bilgileri kontrol ediniz. (Süre sadece rakam olmalıdır)");
                }
            }
        });

        btnEkspertiz.addActionListener(e -> {
            int viewRow = tablo.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Ekspertiz yapılacak aracı seçin!"); return; }
            int modelRow = tablo.convertRowIndexToModel(viewRow);
            int id = (int) tabloModeli.getValueAt(modelRow, 0);

            String mevcutTemizlik = (String) tabloModeli.getValueAt(modelRow, 4);
            String mevcutSigorta = (String) tabloModeli.getValueAt(modelRow, 5);
            String mevcutHasar = (String) tabloModeli.getValueAt(modelRow, 6);

            JPanel ekspertizPanel = new JPanel(new BorderLayout(10, 10));

            JPanel pnlUstG = new JPanel(new GridLayout(2, 2, 5, 5));
            pnlUstG.setBorder(BorderFactory.createTitledBorder("Genel Araç Durumu"));
            JComboBox<String> cmbTemizlik = new JComboBox<>(new String[]{"Temiz", "Kirli"});
            cmbTemizlik.setSelectedItem(mevcutTemizlik);
            JComboBox<String> cmbSigorta = new JComboBox<>(new String[]{"Yapıldı", "Eksik/Tarihi Geçmiş"});
            cmbSigorta.setSelectedItem(mevcutSigorta);
            pnlUstG.add(new JLabel("Araç Temizliği:")); pnlUstG.add(cmbTemizlik);
            pnlUstG.add(new JLabel("Sigorta & Bakım:")); pnlUstG.add(cmbSigorta);

            JPanel pnlHasar = new JPanel(new GridLayout(4, 3, 10, 10));
            pnlHasar.setBorder(BorderFactory.createTitledBorder("Ekspertiz (Hasarlı Parçaları İşaretleyin)"));

            JCheckBox[] parcalar = {
                    new JCheckBox("Ön Tampon"), new JCheckBox("Arka Tampon"), new JCheckBox("Kaput"),
                    new JCheckBox("Tavan"), new JCheckBox("Bagaj Kapağı"), new JCheckBox("Farlar / Aynalar"),
                    new JCheckBox("Sağ Ön Çamurluk"), new JCheckBox("Sol Ön Çamurluk"), new JCheckBox("Sağ Ön Kapı"),
                    new JCheckBox("Sol Ön Kapı"), new JCheckBox("Sağ Arka Kapı"), new JCheckBox("Sol Arka Kapı")
            };

            for(JCheckBox cb : parcalar) {
                if(mevcutHasar.contains(cb.getText())) { cb.setSelected(true); }
                pnlHasar.add(cb);
            }

            ekspertizPanel.add(pnlUstG, BorderLayout.NORTH);
            ekspertizPanel.add(pnlHasar, BorderLayout.CENTER);

            if (JOptionPane.showConfirmDialog(this, ekspertizPanel, "Araç Detaylı Ekspertiz Formu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                StringBuilder hasarRaporu = new StringBuilder();
                for(JCheckBox cb : parcalar) {
                    if(cb.isSelected()) { hasarRaporu.append(cb.getText()).append(", "); }
                }

                String sonHasar = hasarRaporu.length() > 0 ? hasarRaporu.substring(0, hasarRaporu.length() - 2) : "Hasarsız";

                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE \"Oto_KAR_Araç_Kiralama\" SET \"Temizlik\" = ?, \"Sigorta_Bakim\" = ?, \"Hasar\" = ? WHERE \"İd\" = ?")) {
                    pstmt.setString(1, cmbTemizlik.getSelectedItem().toString());
                    pstmt.setString(2, cmbSigorta.getSelectedItem().toString());
                    pstmt.setString(3, sonHasar);
                    pstmt.setInt(4, id);
                    pstmt.executeUpdate();

                    tabloyuYenile();
                    Veritabani.sistemeLogYaz("EKSPERTİZ GÜNCELLENDİ: Araç ID " + id + " -> " + sonHasar);
                    JOptionPane.showMessageDialog(this, "Ekspertiz raporu güncellendi ve sisteme işlendi.");
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        btnIade.addActionListener(e -> {
            int viewRow = tablo.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Araç iade almak için lütfen tablodan araç seçin."); return; }

            int modelRow = tablo.convertRowIndexToModel(viewRow);
            if (tabloModeli.getValueAt(modelRow, 3).equals("Kiralamada")) {
                int id = (int) tabloModeli.getValueAt(modelRow, 0);
                aracSutunGuncelle(id, "\"Durum \"", "Boşta");
                Veritabani.sistemeLogYaz("İADE ALINDI: Araç ID " + id);
                JOptionPane.showMessageDialog(this, "Araç iade alındı ve boşa çıktı.");
            } else {
                JOptionPane.showMessageDialog(this, "Seçilen araç zaten boşta!");
            }
        });

        return panel;
    }

    // --- YENİ 2. SEKME: AKTİF KİRALAMALAR VE CANLI TAKİP ---
    private JPanel pnlAktifKiralamalar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        String[] kolonlar = {"Araç Modeli", "Müşteri", "Telefon", "Çıkış Zamanı", "Kira Süresi", "Beklenen Dönüş", "Kalan Süre / Durum"};
        DefaultTableModel aktifModel = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable aktifTablo = new JTable(aktifModel);
        aktifTablo.setRowHeight(30);
        aktifTablo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        aktifTablo.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(aktifTablo), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlAlt.setBackground(Color.WHITE);
        JButton btnYenile = new JButton("Süreleri ve Durumları Yenile");
        btnYenile.setBackground(new Color(41, 128, 185));
        btnYenile.setForeground(Color.WHITE);
        pnlAlt.add(btnYenile);
        panel.add(pnlAlt, BorderLayout.SOUTH);

        // Canlı süre hesaplama motoru
        Runnable aktifleriCek = () -> {
            aktifModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date suan = new Date();

            try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                 Statement stmt = conn.createStatement();
                 // Sadece durumu 'Kiralamada' olan araçları çekiyoruz
                 ResultSet rsArac = stmt.executeQuery("SELECT * FROM \"Oto_KAR_Araç_Kiralama\" WHERE \"Durum \" = 'Kiralamada'")) {

                while (rsArac.next()) {
                    String model = rsArac.getString("Model Türü");

                    // Bu araca ait en son kesilen faturayı buluyoruz
                    try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM satis_faturalari WHERE arac_model = ? ORDER BY id DESC LIMIT 1")) {
                        pstmt.setString(1, model);
                        ResultSet rsFatura = pstmt.executeQuery();

                        if (rsFatura.next()) {
                            String musteri = rsFatura.getString("musteri_adi");
                            String tel = rsFatura.getString("telefon");
                            String islemTarihi = rsFatura.getString("islem_tarihi");
                            int sureSaat = rsFatura.getInt("kiralama_suresi");

                            // Tarih ve Zaman Hesaplamaları
                            Date baslangic = sdf.parse(islemTarihi);
                            long bitisMillis = baslangic.getTime() + (sureSaat * 3600000L); // Saati milisaniyeye çevirip ekledik
                            Date bitis = new Date(bitisMillis);
                            String bitisTarihiStr = sdf.format(bitis);

                            long farkMillis = bitisMillis - suan.getTime();
                            String durumMesaji;

                            if (farkMillis > 0) {
                                // Süresi var
                                long saat = farkMillis / 3600000;
                                long dk = (farkMillis % 3600000) / 60000;
                                durumMesaji = "🟢 " + saat + " saat " + dk + " dk kaldı";
                            } else {
                                // Süresi geçmiş (Ceza Durumu)
                                long gecikenMillis = Math.abs(farkMillis);
                                long saat = gecikenMillis / 3600000;
                                long dk = (gecikenMillis % 3600000) / 60000;
                                durumMesaji = "🔴 " + saat + " saat " + dk + " dk GECİKTİ!";
                            }

                            aktifModel.addRow(new Object[]{model, musteri, tel, islemTarihi, sureSaat + " Saat", bitisTarihiStr, durumMesaji});
                        }
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        };

        btnYenile.addActionListener(e -> aktifleriCek.run());
        aktifleriCek.run(); // Paneli açarken ilk veriyi yükle

        return panel;
    }

    // --- 3. SEKME: FATURALAR VE SATIŞ GEÇMİŞİ ---
    private JPanel pnlFaturalar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] kolonlar = {"Tarih", "Müşteri Adı", "TC No", "Ehliyet No", "Telefon", "Araç Modeli", "Süre", "Toplam", "Ekspertiz Özeti"};
        faturaModeli = new DefaultTableModel(kolonlar, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        faturaTablosu = new JTable(faturaModeli);
        panel.add(new JScrollPane(faturaTablosu), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout());
        JButton btnFaturaGoruntule = new JButton("Seçili Faturayı Ekranda Görüntüle");
        btnFaturaGoruntule.setBackground(new Color(41, 128, 185)); btnFaturaGoruntule.setForeground(Color.WHITE);

        JButton btnExcel = new JButton("Seçili Faturayı Excel'e Aktar (Detaylı)");
        btnExcel.setBackground(new Color(39, 174, 96)); btnExcel.setForeground(Color.WHITE);

        pnlAlt.add(btnFaturaGoruntule); pnlAlt.add(btnExcel);
        panel.add(pnlAlt, BorderLayout.SOUTH);

        btnFaturaGoruntule.addActionListener(e -> {
            int secili = faturaTablosu.getSelectedRow();
            if (secili == -1) { JOptionPane.showMessageDialog(this, "Lütfen tablodan görüntülemek istediğiniz faturayı seçin."); return; }

            String tarih = (String) faturaModeli.getValueAt(secili, 0);
            String musteriAd = (String) faturaModeli.getValueAt(secili, 1);
            String tcNo = (String) faturaModeli.getValueAt(secili, 2);
            String ehliyetNo = (String) faturaModeli.getValueAt(secili, 3);
            String telefon = (String) faturaModeli.getValueAt(secili, 4);
            String model = (String) faturaModeli.getValueAt(secili, 5);
            int sure = (int) faturaModeli.getValueAt(secili, 6);

            int genelToplam = Integer.parseInt(faturaModeli.getValueAt(secili, 7).toString().replace(" ₺", "").trim());
            String hasar = (String) faturaModeli.getValueAt(secili, 8);

            int araToplam = (int) (genelToplam / 1.20);
            int kdv = genelToplam - araToplam;

            String htmlSozlesme = "<html><body style='font-family: Arial, sans-serif; width: 350px; padding: 10px;'>"
                    + "<h2 style='color: #2980b9; text-align: center; margin-bottom: 2px;'>ANADOLU LOJİSTİK A.Ş.</h2>"
                    + "<p style='color: #7f8c8d; text-align: center; font-size: 11px; margin-top: 0;'>Resmi Araç Kiralama Sözleşmesi (KOPYA NÜSHA)</p>"
                    + "<hr style='border: 1px solid #ecf0f1;'>"
                    + "<table style='width: 100%; font-size: 12px; margin-bottom: 10px;'>"
                    + "<tr><td><b>İşlem Tarihi:</b></td><td style='text-align: right;'>" + tarih + "</td></tr>"
                    + "<tr><td><b>İşlemi Yapan:</b></td><td style='text-align: right;'>" + Veritabani.aktifKullanici + "</td></tr>"
                    + "</table>"
                    + "<h3 style='background-color: #ecf0f1; padding: 5px; font-size: 13px;'>Müşteri Bilgileri</h3>"
                    + "<table style='width: 100%; font-size: 12px;'>"
                    + "<tr><td><b>Ad Soyad:</b></td><td style='text-align: right;'>" + musteriAd + "</td></tr>"
                    + "<tr><td><b>TC No:</b></td><td style='text-align: right;'>" + tcNo + "</td></tr>"
                    + "<tr><td><b>Ehliyet No:</b></td><td style='text-align: right;'>" + ehliyetNo + "</td></tr>"
                    + "<tr><td><b>Telefon:</b></td><td style='text-align: right;'>" + telefon + "</td></tr>"
                    + "</table>"
                    + "<h3 style='background-color: #ecf0f1; padding: 5px; font-size: 13px;'>Araç Detayları</h3>"
                    + "<table style='width: 100%; font-size: 12px;'>"
                    + "<tr><td><b>Model:</b></td><td style='text-align: right;'>" + model + "</td></tr>"
                    + "<tr><td><b>Teslimat Ekspertizi:</b></td><td style='text-align: right; color:#c0392b;'><b>" + hasar + "</b></td></tr>"
                    + "<tr><td><b>Kira Süresi:</b></td><td style='text-align: right;'>" + sure + " Saat</td></tr>"
                    + "</table>"
                    + "<hr style='border: 1px dashed #bdc3c7; margin-top: 15px; margin-bottom: 15px;'>"
                    + "<table style='width: 100%; font-size: 14px;'>"
                    + "<tr><td>Ara Toplam:</td><td style='text-align: right;'>" + araToplam + " ₺</td></tr>"
                    + "<tr><td>KDV (%20):</td><td style='text-align: right;'>" + kdv + " ₺</td></tr>"
                    + "<tr><td colspan='2'><hr style='border: 1px solid #34495e;'></td></tr>"
                    + "<tr><td style='color: #c0392b;'><b>GENEL TOPLAM:</b></td><td style='text-align: right; color: #c0392b; font-size: 18px;'><b>" + genelToplam + " ₺</b></td></tr>"
                    + "</table>"
                    + "</body></html>";

            JEditorPane faturapaneli = new JEditorPane("text/html", htmlSozlesme);
            faturapaneli.setEditable(false); faturapaneli.setBackground(Color.WHITE);
            JOptionPane.showMessageDialog(this, new JScrollPane(faturapaneli), "Fatura Görüntüleyici", JOptionPane.PLAIN_MESSAGE);
        });

        btnExcel.addActionListener(e -> {
            int row = faturaTablosu.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Fatura seçin!"); return; }

            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = new File(chooser.getSelectedFile() + "_Fatura.csv");
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                    bw.write('\ufeff');
                    bw.write("ANADOLU LOJİSTİK A.Ş. - RESMİ KİRALAMA FATURASI\n");
                    bw.write("--------------------------------------------------\n");
                    bw.write("İşlem Tarihi;" + faturaModeli.getValueAt(row, 0) + "\n");
                    bw.write("Personel;" + Veritabani.aktifKullanici + "\n\n");
                    bw.write("Müşteri Adı;" + faturaModeli.getValueAt(row, 1) + "\n");
                    bw.write("TC No;" + faturaModeli.getValueAt(row, 2) + "\n");
                    bw.write("Ehliyet No;" + faturaModeli.getValueAt(row, 3) + "\n");
                    bw.write("Telefon;" + faturaModeli.getValueAt(row, 4) + "\n");
                    bw.write("Araç Modeli;" + faturaModeli.getValueAt(row, 5) + "\n");
                    bw.write("Kiralama Süresi;" + faturaModeli.getValueAt(row, 6) + " Saat\n");
                    bw.write("Teslimat Ekspertizi;" + faturaModeli.getValueAt(row, 8) + "\n");
                    bw.write("--------------------------------------------------\n");

                    int toplam = Integer.parseInt(faturaModeli.getValueAt(row, 7).toString().replace(" ₺", "").trim());
                    int kdv = (int)(toplam * 0.20);
                    bw.write("Ara Toplam;" + (toplam - kdv) + " ₺\n");
                    bw.write("KDV (%20);" + kdv + " ₺\n");
                    bw.write("GENEL TOPLAM;" + toplam + " ₺\n");

                    JOptionPane.showMessageDialog(this, "Fatura başarıyla Excel formatında dışa aktarıldı.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        faturalariCek();
        return panel;
    }

    // --- 4. SEKME: PROFİL VE ÇIKIŞ ---
    private JPanel pnlProfilOlustur() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        JLabel lblProfil = new JLabel("Profil Yükleniyor...");
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM kullanicilar WHERE kullanici_adi = ?")) {
            pstmt.setString(1, Veritabani.aktifKullanici); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                lblProfil.setText("<html><center><h1 style='color:#e67e22;'>Personel Kimlik Kartı</h1><hr><table style='font-size:14px;' cellpadding='5'>"
                        + "<tr><td align='right'><b>Kullanıcı Adı:</b></td><td>" + rs.getString("kullanici_adi") + "</td></tr>"
                        + "<tr><td align='right'><b>Şifre:</b></td><td>" + rs.getString("sifre") + "</td></tr>"
                        + "<tr><td align='right'><b>Telefon:</b></td><td>" + rs.getString("telefon") + "</td></tr>"
                        + "<tr><td align='right'><b>E-Posta:</b></td><td>" + rs.getString("eposta") + "</td></tr>"
                        + "<tr><td align='right'><b>Sistem Yetkisi:</b></td><td>" + rs.getString("rol") + "</td></tr></table><br><br><i>Tüm işlemleriniz loglanmaktadır.</i></center></html>");}
        } catch (SQLException e) { e.printStackTrace(); }

        JButton btnCikis = new JButton("Sistemden Güvenli Çıkış Yap");
        btnCikis.setBackground(new Color(192, 57, 43)); btnCikis.setForeground(Color.WHITE);

        btnCikis.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Çıkış yapmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION) == 0){

                Veritabani.sistemeLogYaz("MESAİ BİTİŞİ (Sistemden çıkış yapıldı)");

                Veritabani.aktifKullanici = "";
                this.dispose();
                new GirisEkrani().setVisible(true);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblProfil, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(20,0,0,0); panel.add(btnCikis, gbc);
        return panel;
    }

    // --- YARDIMCI VERİTABANI METOTLARI ---
    private void tabloyuYenile() {
        tabloModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM \"Oto_KAR_Araç_Kiralama\"")) {
            while (rs.next()) {
                tabloModeli.addRow(new Object[]{rs.getInt("İd"), rs.getString("Model Türü"), rs.getInt("Saatlik Fiyat") + " ₺", rs.getString("Durum "), rs.getString("Temizlik"), rs.getString("Sigorta_Bakim"), rs.getString("Hasar")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void faturalariCek() {
        if(faturaModeli != null) faturaModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM satis_faturalari WHERE personel = '" + Veritabani.aktifKullanici + "' ORDER BY id DESC")) {
            while (rs.next()) {
                faturaModeli.addRow(new Object[]{
                        rs.getString("islem_tarihi"),
                        rs.getString("musteri_adi"),
                        rs.getString("tc_no"),
                        rs.getString("ehliyet_no"),
                        rs.getString("telefon"),
                        rs.getString("arac_model"),
                        rs.getInt("kiralama_suresi"),
                        rs.getInt("toplam_tutar") + " ₺",
                        rs.getString("hasar_durumu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void aracSutunGuncelle(int id, String sutunAdi, String yeniDeger) {
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE \"Oto_KAR_Araç_Kiralama\" SET " + sutunAdi + " = ? WHERE \"İd\" = ?")) {
            pstmt.setString(1, yeniDeger); pstmt.setInt(2, id); pstmt.executeUpdate(); tabloyuYenile();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}