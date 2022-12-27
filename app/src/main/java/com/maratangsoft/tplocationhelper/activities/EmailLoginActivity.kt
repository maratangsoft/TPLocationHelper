package com.maratangsoft.tplocationhelper.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.maratangsoft.tplocationhelper.G
import com.maratangsoft.tplocationhelper.R
import com.maratangsoft.tplocationhelper.databinding.ActivityEmailLoginBinding
import com.maratangsoft.tplocationhelper.model.UserAccount

class EmailLoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEmailLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        binding.btnSignIn.setOnClickListener { clickSignIn() }
    }

    //업버튼 클릭시에 액티비티를 종료
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignIn(){
        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()

        //Firestore DB에서 이메일 등록여부 확인
        val db = FirebaseFirestore.getInstance()
        db.collection("emailUsers")
            .whereEqualTo("email",email)
            .whereEqualTo("password",password)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0){
                    //로그인 성공. 회원정보를 다른 Activity에서도 사용할 가능성이 있으므로 전역변수처럼 사용가능한 변수에 저장하기
                    val id = it.documents[0].id //document의 랜덤한 식별자
                    G.userAccount.id = id
                    G.userAccount.email = email

                    //다른 액티비티로 넘어가면서 task(켜진 액티비티가 모여있는 스택)에 있는 모든 액티비티들을 제거하고 새로운 task 시작
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }else{
                    AlertDialog.Builder(this).setMessage("이메일 혹은 비밀번호가 정확하지 않습니다.").show()
                    binding.etEmail.requestFocus()
                    binding.etEmail.selectAll()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "서버오류: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}