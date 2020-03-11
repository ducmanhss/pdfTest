package com.tools.files.myreader.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.artifex.mupdf.viewer.DocumentActivity
//import com.artifex.mupdf.viewer.DocumentActivity
//import com.folioreader.FolioReader
import com.google.android.material.navigation.NavigationView
import com.hanlyjiang.library.fileviewer.tbs.TBSFileViewActivity
import com.tools.files.myreader.BuildConfig
import com.tools.files.myreader.R
import com.tools.files.myreader.adapter.FileAdapter
import com.tools.files.myreader.adapter.FileRecentAdapter
import com.tools.files.myreader.adapter.ToolMainAdapter
import com.tools.files.myreader.base.BaseActivity
import com.tools.files.myreader.fragment.BottomSheetMenuFragment
import com.tools.files.myreader.helper.MainHelper
import com.tools.files.myreader.model.File
import com.tools.files.myreader.model.FileRc
import com.tools.files.myreader.model.Format
import com.tools.files.myreader.model.Menu
import com.tools.files.myreader.util.*
import com.tools.files.myreader.util.util.ImageFetcher
import com.tools.files.myreader.viewmodel.FileRecentViewModel
import com.tools.files.myreader.viewmodel.FileViewModel
import com.tools.files.myreader.viewmodel.ViewModelUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseActivity(), FileAdapter.OnItemFileClickListener,
    FileRecentAdapter.OnItemFileRecentClickListener, ToolMainAdapter.OnItemToolClickListener {

    private var helper: MainHelper = MainHelper()
    private lateinit var fileViewModel: FileViewModel
    private lateinit var fileRecentViewModel: FileRecentViewModel
    private var list: ArrayList<File> = ArrayList()
    private lateinit var listRc: ArrayList<FileRc>

    private lateinit var st: SharedPreferencesUtil
    private var isGrid:Boolean=false
    private var isHorizontal:Boolean=false
    private var isEpub:Boolean=false
    private var isPdf:Boolean=false
    private var isText:Boolean=false
    private var isFb2:Boolean=false

    private lateinit var menu: Menu
    private lateinit var showMenu: BottomSheetMenuFragment

    private lateinit var adapterFile: FileAdapter
    private lateinit var adapterFileRc: FileRecentAdapter


    private val IMAGE_CACHE_DIR = "thumbs";

    private var mImageThumbSize:Int = 0
    private var mImageThumbSpacing:Int =0
//    private ImageAdapter mAdapter;
    private lateinit var mImageFetcher: ImageFetcher


    private val DISK_CACHE_SUBDIR = "thumbnails";

//    lateinit var searchView: SearchView
//    lateinit var menuItem: MenuItem

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
//        CacheUtil.getInstance().init()
        navigationViewInit()
        setupTools()
        event()


    }

    private fun event() {
//        srl_main.setOnRefreshListener(object :SwipeRefreshLayout.OnRefreshListener{
//            override fun onRefresh() {
//                fileViewModel.deleteAllFile()
//                helper.allFileLowerThanApi29(this@MainActivity, fileViewModel)
//                Handler().postDelayed(object :Runnable{
//                    override fun run() {
//                        srl_main.isRefreshing=false
//                    }
//
//                },3500)
//            }
//
//        })


        sv_main.setOnQueryTextListener(queryText)
        tv_scan.setOnClickListener { v ->
            scanData()
        }
        iv_grid.setOnClickListener { v ->
            updateAdapterPreView(list, true)
            st.isGridMode=true
        }
        iv_list.setOnClickListener { v ->

            updateAdapterPreView(list, false)
            st.isGridMode=false
        }

    }

    private fun scanData() {
//        rl_loadview_prv.visibility = View.VISIBLE
//        rv_preview_main.visibility=View.GONE
        rv_preview_main.onFlingListener
        if (FileUtils.deleteDirectoryPreview()) {
            Toast.makeText(this, "done", Toast.LENGTH_SHORT).show()
        } else {
            false
        }
        fileViewModel.deleteAllFile()

        Log.e("zzzzz", "  " + list.size.toString())
//        adapterFile.updateList(list)
//        helper.allFileLowerThanApi29(this,fileViewModel)
//        Handler().postDelayed(object :Runnable{
//            override fun run() {
//                rl_loadview_prv.visibility = View.GONE
//                rv_preview_main.visibility=View.VISIBLE
//            }
//
//        },3000)

    }

    private fun setupTools() {

        val lst = helper.dataTool()
        val adapter = ToolMainAdapter(this, lst)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_toolmain.setHasFixedSize(true)
        rv_toolmain.isNestedScrollingEnabled = false
        rv_toolmain.layoutManager = layoutManager
        rv_toolmain.adapter = adapter


    }

    private fun navigationViewInit() {

        initSetting()
        initData()

//        toolbar_main.setTitle()

        setSupportActionBar(toolbar_main)
        supportActionBar!!.setTitle(R.string.home)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar_main,
            R.string.nav_open,
            R.string.nav_close
        )
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(p0: MenuItem): Boolean {
                when (p0.itemId) {

                    R.id.settings -> {
                        startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                    }
                    R.id.rate -> {
                        rateApp()
                    }

                    R.id.other_applications -> {
                        moreApp()
                    }
                    R.id.share -> {
                        shareApp()
                    }
                    R.id.about -> {
                        startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                    }


                }
                drawer_layout.closeDrawer(GravityCompat.START)
                return true
            }

        })

    }

    override fun onResume() {
        super.onResume()
//        if (isGrid!=st.isGridMode){
//            isGrid=st.isGridMode
//            updateAdapterPreView(list,st.isGridMode)
//            Log.d("addd","okg")
//        }else if(isEpub!=st.isEPUB
//            ||isPdf!=st.isPDF
//            ||isText!=st.isTEXT
//            ||isFb2!=st.isFB2){
////            fileViewModel.deleteAllFile()
//            initData()
//            isEpub=st.isEPUB
//            isPdf=st.isPDF
//            isText=st.isTEXT
//            isFb2=st.isFB2
//            Log.d("addd","oki")
//        }else{
//            Log.d("addd","no")
//        }
    }
    private fun initSetting() {
        st = SharedPreferencesUtil.getInstance()
        st.init()
        isGrid=st.isGridMode
        isHorizontal=st.isHorizoltalScroll
        isEpub=st.isHorizoltalScroll
        isPdf=st.isPDF
        isText=st.isTEXT
        isFb2=st.isFB2
//        st.sharedPreferences.registerOnSharedPreferenceChangeListener(object :
//            SharedPreferences.OnSharedPreferenceChangeListener {
//            override fun onSharedPreferenceChanged(
//                sharedPreferences: SharedPreferences?,
//                key: String?
//            ) {
//                if (sharedPreferences!!.equals(Constant.MODE_GRID)) {
//                    updateAdapterPreView(list, st.isGridMode)
//                } else if (sharedPreferences!!.equals(Constant.MODE_HIDE_EPUB)
//                    || sharedPreferences!!.equals(Constant.MODE_HIDE_PDF)
//                    || sharedPreferences!!.equals(Constant.MODE_HIDE_TEXT)
//                    || sharedPreferences!!.equals(Constant.MODE_HIDE_FB2)
//                ) {
//                    initData()
//                }
//            }
//
//        })
    }

    private fun shareApp() {
        try {
            val start = System.currentTimeMillis()
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            var sb = StringBuilder()
            sb.append("\nLet me recommend you this application\n\n")
            sb.append("https://play.google.com/store/apps/details?id=")
            sb.append(BuildConfig.APPLICATION_ID + "\n\n")
            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString())
            startActivity(Intent.createChooser(shareIntent, "choose one"))
            Log.d("ststst", "${System.currentTimeMillis() - start}")
        } catch (unused: Exception) {
            unused.printStackTrace()
        }
    }


    private fun moreApp() {


//        startActivity(
//            Intent(this, SuggestActivity::class.java)
//        )

        try {

            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("market://search?q=pub:\"FX+Studio+®\"")
                )
            )

        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("https://play.google.com/store/apps/developer?id=FX+Studio+®")
                )
            )
        }
    }


    private fun rateApp() {
        try {
            val uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }

    private fun initData() {

        fileViewModel = ViewModelUtils(this).initFileViewModel()
        fileRecentViewModel = ViewModelUtils(this).initFileRecentViewModel()
//if (st.isEPUB!=false|| st.isPDF!=false||st.isTEXT!=false||st.isFB2!=false)
//        fileViewModel.getAllFilePick(st.isEPUB, st.isPDF, st.isTEXT, st.isFB2)!!.observe(

//        fileViewModel.getAllFileByExtension(ExtensionUltis.Ex_FB2,ExtensionUltis.Ex_TXT).observe(

        fileViewModel.getAllFile().observe(
            this,
            object : Observer<List<File>> {
                override fun onChanged(t: List<File>?) {
                    list = t as ArrayList<File>
                    if (list.size <= 0) {
                        tv_nodt_2.visibility = View.VISIBLE
                        Log.d("addd","add")
                        helper.allFileLowerThanApi29(this@MainActivity, fileViewModel)
                    } else {
                        tv_nodt_2.visibility = View.GONE
                    }
                    Log.d("addd","list"+list.size)
                    updateAdapterPreView(list, st.isGridMode)
                }

            })
        fileRecentViewModel.getAllFile().observe(this, object : Observer<List<FileRc>> {
            override fun onChanged(t: List<FileRc>?) {
                listRc = t as ArrayList<FileRc>
                if (listRc.size <= 0) {
                    tv_nodt_1.visibility = View.VISIBLE

                } else {
                    tv_nodt_1.visibility = View.GONE
                }
                updateAdapterRecent(listRc)
            }

        })


    }


    private fun updateAdapterRecent(list: ArrayList<FileRc>) {

        list.reverse()
        adapterFileRc = FileRecentAdapter(this, list)
        val layoutManager = GridLayoutManager(this, 1)

        rv_recent_main.setHasFixedSize(true)
        rv_recent_main.isNestedScrollingEnabled = false
        rv_recent_main.layoutManager = layoutManager
        rv_recent_main.adapter = adapterFileRc
    }

    private fun updateAdapterPreView(list: ArrayList<File>, isGrid: Boolean) {
        Log.d("addd","update")
        adapterFile = FileAdapter(this, list, isGrid)
        val layoutManager = object : GridLayoutManager(this, if (isGrid) 3 else 1) {
            override fun canScrollVertically(): Boolean {
                return true
            }
        }
        rv_preview_main.setHasFixedSize(true)
        rv_preview_main.isNestedScrollingEnabled = false
        rv_preview_main.layoutManager = layoutManager
        rv_preview_main.adapter = adapterFile
        ViewCompat.setNestedScrollingEnabled(rv_preview_main, false)
    }

    public fun renameInList(file: File, newName: File): Boolean {
        try {
            val i = list.indexOf(file)
            if (i != -1) {
                list.set(i, newName)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    public fun updateDataPreview(file: File) {

        try {
            val i = list.indexOf(file)
            if (i != -1) {
                adapterFile.updateItem(i, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onFavouriteClick(file: File) {
    }

    override fun onItemClick(file: File) {

        var isRecent = false
        if (listRc.size > 0) {
            listRc.forEach {
                Log.d("aaaaaa", "12")
                if (it.path.equals(file.path)) {
                    Log.d("aaaaaa", "13")
                    isRecent = true
                }
            }
            if (!isRecent) {
                fileRecentViewModel.insertFile(
                    FileRc(
                        file.name,
                        file.path,
                        file.date,
                        file.size,
                        file.etension,
                        file.format,
                        System.currentTimeMillis()
                    )
                )
            }
        } else {
            fileRecentViewModel.insertFile(
                FileRc(
                    file.name,
                    file.path,
                    file.date,
                    file.size,
                    file.etension,
                    file.format,
                    System.currentTimeMillis()
                )
            )
        }
        eventClick(file)

    }


    override fun onMenuClick(file: File) {
        menu = Menu(Constant.BTS_PREVIEW, fileViewModel, file)
        showMenu = BottomSheetMenuFragment(menu, this, null)
        showMenu.show(supportFragmentManager, showMenu.tag)
    }

    override fun onSaveImageTemp(file: File) {
        fileViewModel.updateFile(file)
    }

    override fun onFavouriteRcClick(file: FileRc) {
    }

    override fun onItemRcClick(file: FileRc) {
        val fileConvert = File(
            file.name,
            file.path,
            file.date,
            file.size,
            file.etension,
            file.format,
            file.timeRead,
            file.isReading,
            file.numberPages,
            file.isFavourite
        )
        eventClick(fileConvert)
    }

    override fun onMenuRcClick(file: FileRc) {
//        fileRecentViewModel.deleteFileById(file.id)

//        menu = Menu(Constant.BTS_RECENT, fileRecentViewModel, file)
//        showMenu = BottomSheetMenuFragment(menu,this,null)
//        showMenu.show(supportFragmentManager, showMenu.tag)
        showDialogRecent(file)
    }


    override fun onClick(format: Format) {
        when (format.name) {
            ExtensionUltis.WORD -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.WORD
                    )
                )
                Toast.makeText(this, ExtensionUltis.WORD, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.PDF -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.PDF
                    )
                )
                Toast.makeText(this, ExtensionUltis.PDF, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.TEXT -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.TEXT
                    )
                )
                Toast.makeText(this, ExtensionUltis.TEXT, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.EPUB -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.EPUB
                    )
                )
                Toast.makeText(this, ExtensionUltis.EPUB, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.MOBI -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.MOBI
                    )
                )
                Toast.makeText(this, ExtensionUltis.MOBI, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.DJVU -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.DJVU
                    )
                )
                Toast.makeText(this, ExtensionUltis.DJVU, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.ODT -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.ODT
                    )
                )
                Toast.makeText(this, ExtensionUltis.ODT, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.RTF -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.RTF
                    )
                )
                Toast.makeText(this, ExtensionUltis.RTF, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.ZIP -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.ZIP
                    )
                )
                Toast.makeText(this, ExtensionUltis.ZIP, Toast.LENGTH_SHORT).show()
            }
            ExtensionUltis.FB2 -> {
                startActivity(
                    Intent(this, FileClassificationActivity::class.java).putExtra(
                        ExtensionUltis.INTENT_EXTENSION,
                        ExtensionUltis.FB2
                    )
                )
                Toast.makeText(this, ExtensionUltis.FB2, Toast.LENGTH_SHORT).show()
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class LoadDataAsync() : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            helper.allFileLowerThanApi29(this@MainActivity, fileViewModel)
            return null
        }

        override fun onPreExecute() {

        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class CheckDataByPathAsync() : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean {
            var isRecent = true
            if (listRc.size > 0) {
                listRc.forEach {
                    Log.d("aaaaaa", "12")
                    if (it.path.equals(params[0])) {
                        Log.d("aaaaaa", "13")
                        isRecent = false
//                fileRecentViewModel.insertFile(
//                    FileRc(
//                        file.name,
//                        file.path,
//                        file.date,
//                        file.size,
//                        file.etension,
//                        file.format
//                    )
//                )
                    }
                }
            }
            return isRecent
        }

        override fun onPreExecute() {

        }
    }


    private fun startDocumentActivity(file: File) {
        val intent = Intent(this, DocumentActivity::class.java)
        // API>=21: intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) /* launch as a new document */
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.fromFile(java.io.File(file.path))
        startActivity(intent)
    }

    private fun startReadTextActivity(file: File) {
        val intent = Intent(this, ReadTextActivity::class.java)
        intent.putExtra(ExtensionUltis.INTENT_EXTENSION_TEXT, file.path)
        startActivity(intent)
    }

    private fun startFolioReader(path: String) {
//        val folioReader = FolioReader.get()
//        folioReader.openBook(path)
    }

    private fun startTTBSFileViewActivity(path: String) {

        TBSFileViewActivity.viewFile(this, java.io.File(path).absolutePath);

//            TBSFileViewActivity.viewFile(this, getFileByPath());

//            TBSFileViewActivity.viewFile(this, getFilePath("TestExcel.xls"));
    }

    private fun eventClick(file: File) {
        if (file.etension.equals(ExtensionUltis.Ex_TXT)
            || file.etension.equals(ExtensionUltis.Ex_HTML)
            || file.etension.equals(ExtensionUltis.Ex_XML)
            || file.etension.equals(ExtensionUltis.Ex_JAVA)
        ) {
            startReadTextActivity(file)
//        } else if (file.etension.equals(ExtensionUltis.Ex_EPUB)) {
//            startFolioReader(file.path!!)
//        } else if (file.etension.equals(ExtensionUltis.Ex_DOC)) {
//            startTTBSFileViewActivity(file.path!!)

        } else {
            startDocumentActivity(file)
        }
    }


//    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_main, menu)
//        menuItem = menu.findItem(R.id.action_search)
//        searchView = menuItem.actionView as SearchView
//        searchView.setOnQueryTextListener(queryText)
//        searchView.queryHint = getString(R.string.search_file)
//        super.onCreateOptionsMenu(menu, inflater)
//    }


//    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
//        super.onCreateOptionsMenu(menu)
//        menuInflater.inflate(R.menu.menu_main, menu)
//        menuItem =menu!!.findItem(R.id.action_search)
//        searchView = menuItem.actionView as SearchView
//        searchView.setOnQueryTextListener(queryText)
//        searchView.queryHint = getString(R.string.search_file)
//        return true
//    }

    val queryText = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            val listSearch = list
            if (listSearch.size > 0) {
                searchItem(newText)
            }
            return true
        }

    }

    @SuppressLint("DefaultLocale")
    private fun searchItem(newText: String?) {
        try {
            val arrayList = ArrayList<File>()
            list.forEach {
                if (it.name!!.toLowerCase().contains(newText!!.toLowerCase())) {
                    arrayList.add(it)
                }
            }
            adapterFile.updateList(arrayList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showDialogRecent(file: FileRc) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(file.name)
        builder.setMessage(
            "\n" +
                    getString(R.string.delete_recent_dialog)
        )
        builder.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

            }

        })
        builder.setPositiveButton("YES", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                fileRecentViewModel.deleteFileById(file.id)
//             Log.d("qqxxxx","Delete " +file.path)
            }

        })

        builder.create().show()
    }

    var i = 0
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
            i = i + 1
            if (i >= 2) {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.exit), Toast.LENGTH_SHORT).show()
                Handler().postDelayed(object : Runnable {
                    override fun run() {
                        i = 0
                    }

                }, 2000)
            }
        }
    }

    private fun getExtension(file: File): String {
        try {
            var extension = file.etension
            if (extension.equals(ExtensionUltis.EPUB))
                extension = "epub"
            else if (extension.equals(ExtensionUltis.PDF))
                extension = "pdf"
            else if (extension.equals(ExtensionUltis.ZIP))
                extension = "zip"
            else if (extension.equals(ExtensionUltis.WORD))
                extension = "doc"
            else if (extension.equals(ExtensionUltis.ODT))
                extension = "odt"
            else if (extension.equals(ExtensionUltis.RTF))
                extension = "rtf"
            else if (extension.equals(ExtensionUltis.DJVU))
                extension = "html"
            else if (extension.equals(ExtensionUltis.TEXT)) {
                extension = "txt"
            }
            return extension!!
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}

