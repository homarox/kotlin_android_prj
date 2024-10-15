package com.example.androidtestproject.kotlinOriginal.androidStandardAlone

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName

object The001BasicAndroid {

    /** Context xác định tình trạng hiện tại của ứng dụng hoặc đối tượng.
     *  Nó cung cấp nhiều tính năng hữu ích như new activity instance, access databases, start a service,...
     *  Bạn có thế sử dụng các cách sau để lấy ra context: getApplicationContext(), getContext(), getBaseContext()
     *  or this trong activity hiện tại.
     *
     *  Trong Android, có ba dạng context chính: Application Context, Activity Context và Service Context.
     *  - Application Context là một instance global, nó không bị ảnh hưởng bởi lifecycle của các activities.
     *  Nó thường được sử dụng trong trường hợp bạn cần một context mà không liên quan đến UI hoặc
     *  bạn cần một context mà tồn tại trong suốt lifecycle của ứng dụng.
     *  - Activity Context là một instance cụ thể cho mỗi activity. Nó được tạo ra và hủy đi theo lifecycle của activity.
     *  Nếu bạn cần một context mà liên quan đến UI hoặc bạn cần một context chỉ tồn tại trong lifecycle của một activity,
     *  bạn nên sử dụng Activity Context.
     *  - Service Context là một instance cụ thể cho mỗi service. Giống như Activity Context,
     *  nó cũng được tạo ra và hủy đi theo lifecycle của service. Nếu bạn cần một context mà không liên quan đến UI
     *  và chỉ tồn tại trong lifecycle của một service, bạn nên sử dụng Service Context.
     * */
    fun contextInAndroid01() {
        showCurrentFunctionName()
    }

    /** Trong Android, Intent là một đối tượng quan trọng được sử dụng để chuyển dữ liệu giữa các thành phần ứng dụng
     * (như Activity, Service, BroadcastReceiver) và thực hiện các hành động.
     * Nó hoạt động như một đối tượng gói gọn mô tả một hoạt động sẽ được thực hiện.
     *
     * Các Intent có thể được gửi qua các phương thức như startActivity() hoặc startActivityForResult().
     * Sử dụng Intents là một cách hiệu quả để tăng tính tương tác giữa các thành phần của ứng dụng
     * và giữa các ứng dụng khác nhau trên cùng một thiết bị.
     * */
    fun intentInAndroid02() {
        showCurrentFunctionName()
    }

}