package com.maratangsoft.tplocationhelper.activities

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.maratangsoft.tplocationhelper.R
import com.maratangsoft.tplocationhelper.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바를 액션바로 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        binding.btnSignup.setOnClickListener { clickSignup() }
    }
    //업버튼 클릭할 때 자동 발동하는 콜백 메소드
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignup(){
        //Firebase Firestore DB에 사용자 정보 저장하기 [앱과 firebase 플랫폼 연동]
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()

        //원래는 정규표현식(RegExp)을 이용해서 유효성 검사함. 시간상 pass
        
        //패스워드가 올바른지 확인
        if(password != passwordConfirm){
            AlertDialog.Builder(this).setMessage("비밀번호를 확인해 주세요.").show()
            binding.etPasswordConfirm.selectAll() //써있는 글씨를 모두 선택상태로 바꿔서 쉽게 재입력 가능하게
            return
        }
        //Firestore DB 작업
        val db = FirebaseFirestore.getInstance()

        //이미 가입한 적 있는지 체크
        //필드값 중에 'email' 의 값이 EditText에 입력한 email과 같은 것이 있는지 찾아달라고 요청
        db.collection("emailUsers")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0){
                    AlertDialog.Builder(this).setMessage("이미 가입된 이메일입니다.").show()
                    binding.etEmail.requestFocus() //아래의 selectAll()하려면 포커스가 있어야 함
                    binding.etEmail.selectAll()
                }else{
                    //저장할 데이터들을 하나로 묶기 위해 MutableMap() 사용
                    val user = mutableMapOf<String,String>()
                    user["email"] = email
                    user["password"] = password

                    //별도의 document명을 주지 않으면 랜덤값으로 설정됨. 이 랜덤값을 회원번호의 역할로 사용함
                    //db.collection("emailUsers").document("").set(user)
                    db.collection("emailUsers").add(user)
                        .addOnSuccessListener {
                            AlertDialog.Builder(this).setMessage("축하합니다. 회원가입이 완료되었습니다.")
                                .setPositiveButton("확인") { _, _ -> finish() }
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
}