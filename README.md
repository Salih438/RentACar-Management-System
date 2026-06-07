
# 🚗 Anadolu Lojistik - Araç Kiralama ve Filo Yönetim Sistemi

Bu proje, araç kiralama şirketlerinin tüm operasyonel, finansal ve personel yönetim süreçlerini dijitalleştirmek amacıyla geliştirilmiş kapsamlı bir **Masaüstü (Desktop) Otomasyon Yazılımıdır**. Nesne Yönelimli Programlama (OOP) prensipleriyle tasarlanmış olup, kurumsal seviyede iş kuralları (Business Logic) ve güvenlik önlemleri barındırır.

## 🛠️ Kullanılan Teknolojiler

* **Programlama Dili:** Java (JDK)
* **Kullanıcı Arayüzü (GUI):** Java Swing (Modern Flat Design)
* **Veritabanı:** SQLite
* **Bağlantı Protokolü:** JDBC
* **Mimari Yaklaşım:** OOP, Çok Kanallı İşlem (Multithreading/SwingWorker), MVC Benzeri Katmanlı Mimari

## 🌟 Öne Çıkan Özellikler ve Modüller

### 🔐 1. Rol Tabanlı Erişim Kontrolü (RBAC)
Sistemde 4 farklı yetki seviyesi bulunmaktadır. Her rol, sadece kendi sorumluluk alanındaki modüllere erişebilir:
* **Personel:** Araç kiralama, iade alma, ekspertiz güncelleme ve canlı süre takibi.
* **Yönetici:** Filo performansı görüntüleme, araç ekleme/silme/fiyatlandırma ve personel maaş/prim yönetimi.
* **Patron:** Şirketin genel kasası, net kar/zarar analizleri ve döngüsel vergi/gider ödemeleri.
* **Admin:** Sistemdeki tüm kullanıcıların yetki denetimi, sistem loglarının takibi ve veritabanı yedekleme işlemleri.

### 💰 2. Adil ve Korumalı Prim Algoritması
Personellerin satış performansına göre **kademeli (tiered)** prim hesaplanır. Şirketi zarara uğratacak olası manipülasyonları engellemek amacıyla personelin alacağı prim, **maaşının %30'u** ile sınırlandırılmıştır (Prim Tavanı).

### ⏱️ 3. Canlı Süre ve Gecikme Takibi
"Aktif Kiralamalar" modülü, kirada olan araçların dönüş sürelerini milisaniye bazında hesaplar. Teslim süresi geçen araçlar için otomatik "GECİKTİ" uyarıları vererek vardiya değişimlerindeki olası hataları engeller.

### 🔄 4. Finansal Döngü Kontrolü
Patron ekranında gerçekleştirilen aylık şirket gideri ve vergi ödemeleri sistem tarafından loglanarak takip edilir. Aynı ay içerisinde mükerrer ödeme yapılması sistem tarafından kilitlenerek kasanın (likidite) korunması sağlanır.

### 📄 5. Dinamik HTML Sözleşme ve Excel Çıktısı
Müşteri bilgileri ve 12 parçalık detaylı araç ekspertiz verileri birleştirilerek HTML formatında resmi kiralama sözleşmesi (fatura) üretilir. İstenildiği takdirde tüm fatura veya log geçmişi Excel (`.csv`) formatında dışa aktarılabilir.

### 💾 6. Asenkron Veritabanı Yedekleme
Admin panelinde bulunan gelişmiş yedekleme motoru, `Thread` yapısı kullanarak arayüzü kilitlemeden (UI Freezing) çalışır. Tüm `salih.db` veritabanını o anki tarih damgasıyla şifreler ve yedekler.

## ⚙️ Kurulum ve Çalıştırma

1. Bu depoyu bilgisayarınıza klonlayın:
`git clone https://github.com/Salih438/RentACar-Yonetim-Sistemi.git`

2. Projeyi tercih ettiğiniz bir Java IDE'si ile (IntelliJ IDEA, Eclipse, NetBeans) açın.

3. Projenin bağımlılıkları arasına (Libraries) `sqlite-jdbc` sürücüsünü (driver) eklediğinizden emin olun.

4. `src/Main.java` dosyasını çalıştırın.

5. Sistem ilk kez çalıştırıldığında gerekli veritabanı tablolarını (`salih.db`) otomatik olarak oluşturacaktır.

## 📸 Ekran Görüntüleri
1. Sisteme Giriş Ekranı
<img width="1918" height="1017" alt="sistem_giris" src="https://github.com/user-attachments/assets/67e51d4e-333b-4115-b2d3-83055bb0eeb1" />

2. Patron Ekranı - Kasa ve Kar/Zarar Takibi
<img width="1918" height="1017" alt="patron_kasa" src="https://github.com/user-attachments/assets/504a164a-de33-4331-b5cb-cff4798378d3" />

3. Patron Ekranı - Şirket Giderleri ve Vergi Ödemesi
<img width="1918" height="1018" alt="patron_gider_ödeme" src="https://github.com/user-attachments/assets/4462434a-1b47-41fc-818c-9289965cc060" />

4. Yönetici Ekranı - Operasyon ve Filo Özeti
<img width="1918" height="1017" alt="filo_özeti" src="https://github.com/user-attachments/assets/25098d12-d0ee-4023-9152-576911d3a12a" />

5. Personel Ekranı - Araç Kiralama ve Yönetim
<img width="1918" height="1017" alt="araç_kiralama" src="https://github.com/user-attachments/assets/aa7e1268-f005-4800-83f6-415220abb74d" />

6. Admin Ekranı - Gelişmiş Veritabanı Yedekleme
<img width="1915" height="1017" alt="admin_sistem_yedekleme" src="https://github.com/user-attachments/assets/2f2ad26f-45e9-4a0e-aab4-d454d807dd9a" />

Geliştirici: Salih Balta

Eğitim: Gümüşhane Üniversitesi - Yazılım Mühendisliği

