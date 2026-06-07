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
```bash
   git clone [https://github.com/Salih438/RentACar-Yonetim-Sistemi.git](https://github.com/Salih438/RentACar-Yonetim-Sistemi.git)
