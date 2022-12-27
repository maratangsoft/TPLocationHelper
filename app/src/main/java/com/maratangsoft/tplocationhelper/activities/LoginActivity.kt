package com.maratangsoft.tplocationhelper.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.maratangsoft.tplocationhelper.G
import com.maratangsoft.tplocationhelper.R
import com.maratangsoft.tplocationhelper.databinding.ActivityLoginBinding
import com.maratangsoft.tplocationhelper.model.NidUserInfoResponse
import com.maratangsoft.tplocationhelper.model.UserAccount
import com.maratangsoft.tplocationhelper.network.RetrofitHelper
import com.maratangsoft.tplocationhelper.network.RetrofitService
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //둘러보기 글씨 클릭으로 로그인 없이 메인화면으로 이동
        binding.tvPreview.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //회원가입 버튼 클릭
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        //이메일 로그인 버튼 클릭
        binding.layoutEmail.setOnClickListener {
            startActivity(Intent(this, EmailLoginActivity::class.java))
        }
        binding.btnLoginKakao.setOnClickListener { clickedLoginKakao() }
        binding.btnLoginGoogle.setOnClickListener { clickedLoginGoogle() }
        binding.btnLoginNaver.setOnClickListener { clickedLoginNaver() }

        //카카오 SDK용 키해시 값 얻어오기
        var keyHash = Utility.getKeyHash(this)
        Log.i("keyHash", keyHash)
    }

    private fun clickedLoginKakao(){
        //Kakao Login API를 이용하여 사용자 정보 취득
        
        //로그인 시도한 결과를 받았을 대 발동하는 콜백 메소드를 별도로 만들기
        val callback: (OAuthToken?, Throwable?)->Unit = { token, error ->
            if (error != null){
                Toast.makeText(this, "카카오로그인 실패", Toast.LENGTH_SHORT).show()
            }else{
                UserApiClient.instance.me { user, error ->
                    if (user!=null){
                        val id:String = user.id.toString()
                        val email:String = user.kakaoAccount?.email ?: ""

                        Toast.makeText(this, "아이디: $id, 이메일: $email", Toast.LENGTH_SHORT).show()
                        G.userAccount = UserAccount(id, email)

                        //로그인이 성공했으니 Main 화면으로 전환
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
        
        //카톡로그인 권장. but 카톡이 안 깔려 있다면 카카오계정으로 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        }else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }
    private fun clickedLoginGoogle(){
        // 구글 로그인 화면(액티비티)를 실행하여 결과를 받아와서 사용자정보 취득
        //구글 로그인 옵션객체 생성: Builder 이용
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() //이메일정보를 받을 수 있는 로그인 옵션값
            .build()

        //로그인용 액티비티가 이미 라이브러리에 만들어져 있음. 그거 실행하기만 하면 됨
        //그러니 그 액티비티를 실행시켜 주는 Intent 객체를 소환
        val intent:Intent = GoogleSignIn.getClient(this, signInOptions).signInIntent
        //로그인 결과를 받기 위해 액티비티를 실행
        googleResultLauncher.launch(intent)
    }

    //구글 로그인 화면을 실행시키고 그 결과를 되돌려받는 작업을 관리하는 객체 생성
    private val googleResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //로그인 결과를 가져온 인텐트객체를 소환
        val intent = it.data
        //돌아온 인텐트객체에게 구글 계정정보 빼오기
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        val account = task.result
        val id = account.id ?: ""
        val email = account.email ?: ""

        Toast.makeText(this, "Google 로그인 성공: $email", Toast.LENGTH_SHORT).show()
        G.userAccount = UserAccount(id, email)

        //로그인이 성공했으니 Main 화면으로 전환
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun clickedLoginNaver(){
        //사용자정보를 취득하는 토큰값을 발급받아 REST API 방식으로 사용자정보 취득
        //네이버 개발자센터 가이드 문서 참고: 애플리케이션 등록 완료
        //네아로 SDK 초기화
        NaverIdLoginSDK.initialize(this, "bv59S6ftwsa53oAGr1bP", "veS8AOlMIQ", "${R.string.app_name}")
        //네아로 전용버튼 뷰 사용 대신 직접 로그인 요청 메소드 사용
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
            }
            override fun onSuccess() {
                //사용자 정보를 가져오려면 서버와 HTTP REST API 통신을 해야 함
                //단 필요한 요청파라미터가 있음. 사용자정보에 접속할 수 있는 인증키같은 값: 토큰
                val accessToken = NaverIdLoginSDK.getAccessToken()

                //토큰값을 확인해보기: 토큰값은 1시간마다 갱신됨
                Log.i("token", accessToken.toString())

                RetrofitHelper.getInstance("https://openapi.naver.com/")
                    .create(RetrofitService::class.java)
                    .getNidUserInfo("Bearer $accessToken")
                    .enqueue(object : Callback<NidUserInfoResponse> {
                        override fun onResponse(
                            call: Call<NidUserInfoResponse>,
                            response: Response<NidUserInfoResponse>
                        ) {
                            response.body()?.let {
                                val id = it.response.id
                                val email = it.response.email

                                Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                                G.userAccount = UserAccount(id, email)

                                //로그인이 성공했으니 Main 화면으로 전환
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                        override fun onFailure(call: Call<NidUserInfoResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "회원정보 로딩 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        })
    }
}