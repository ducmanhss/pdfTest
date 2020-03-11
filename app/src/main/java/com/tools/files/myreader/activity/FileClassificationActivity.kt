package com.tools.files.myreader.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
//import com.artifex.mupdf.viewer.DocumentActivity
//import com.folioreader.FolioReader
import com.hanlyjiang.library.fileviewer.tbs.TBSFileViewActivity
import com.tools.files.myreader.R
import com.tools.files.myreader.adapter.FileAdapter
import com.tools.files.myreader.base.BaseActivity
import com.tools.files.myreader.base.GlobalApplication.FILE_DIR
import com.tools.files.myreader.fragment.BottomSheetMenuFragment
import com.tools.files.myreader.model.File
import com.tools.files.myreader.model.FileRc
import com.tools.files.myreader.model.Format
import com.tools.files.myreader.model.Menu
import com.tools.files.myreader.util.*
import com.tools.files.myreader.viewmodel.FileRecentViewModel
import com.tools.files.myreader.viewmodel.FileViewModel
import com.tools.files.myreader.viewmodel.ViewModelUtils
import kotlinx.android.synthetic.main.activity_file_classification.*
import java.security.cert.Extension
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class FileClassificationActivity : BaseActivity(), FileAdapter.OnItemFileClickListener {
    override fun onSaveImageTemp(file: File) {
        fileViewModel.updateFile(file)
    }

    private val TAG = "TBSInit"


    override fun onFavouriteClick(file: File) {
    }

    override fun onItemClick(file: File) {
        if (file.etension.equals(ExtensionUltis.Ex_TXT) || file.etension.equals(ExtensionUltis.Ex_HTML) || file.etension.equals(
                ExtensionUltis.Ex_JAVA
            ) || file.etension.equals(ExtensionUltis.Ex_XML)
        ) {
            startReadTextActivity(file)
//        } else if (file.etension.equals("epub")) {
//            startFolioReader(file.path!!)
//        } else if (file.etension.equals("doc")) {
//            startTTBSFileViewActivity(file.path!!)

        } else {
            startDocumentActivity(file)
        }
//        startTTBSFileViewActivity(file.path!!)
    }

    override fun onMenuClick(file: File) {
        menu = Menu(Constant.BTS_PREVIEW, fileViewModel, file)
        showMenu = BottomSheetMenuFragment(menu, null, this)
        showMenu.show(supportFragmentManager, showMenu.tag)
    }


    private lateinit var adapter: FileAdapter
    private lateinit var fileViewModel: FileViewModel
    private lateinit var fileRecentViewModel: FileRecentViewModel
    private var lst: ArrayList<File> = ArrayList()
    private lateinit var lstRc: ArrayList<FileRc>
    //    private var isGrid: Boolean =true
    private var spr = SharedPreferencesUtil.getInstance()
    private lateinit var menu: Menu
    private lateinit var showMenu: BottomSheetMenuFragment
    private val POPUP_CONSTANT = "mPopup";
    private val POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
//    private var listSearch:ArrayList<File> =ArrayList()

    private lateinit var searchView: SearchView
    private lateinit var menuItem: MenuItem

    var extension = ""


    override fun getLayout(): Int {
        return R.layout.activity_file_classification
    }

    override fun initView() {

        spr.init()
//        Request.requestPermissions(this)
        checkExtension()

        setupToolBar()

        if (!extension.equals(ExtensionUltis.TEXT)) {

            when (extension) {
                ExtensionUltis.EPUB -> {
                    getListBySingleExtension(ExtensionUltis.Ex_EPUB)
                }
                ExtensionUltis.PDF -> {
                    getListBySingleExtension(ExtensionUltis.Ex_PDF)
                }
                ExtensionUltis.FB2 -> {
                    getListBySingleExtension(ExtensionUltis.Ex_FB2)
                }

            }
        } else {

            fileViewModel = ViewModelUtils(this).initFileViewModel()
            fileRecentViewModel = ViewModelUtils(this).initFileRecentViewModel()

            fileViewModel.getAllFileByExtension(
//                ExtensionUltis.Ex_HTML,
                ExtensionUltis.Ex_TXT
//                ExtensionUltis.Ex_JAVA,
//                ExtensionUltis.Ex_XML

//                ExtensionUltis.FB2,
//                ExtensionUltis.Ex_TXT

            )
                .observe(this, object : Observer<List<File>> {
                    override fun onChanged(t: List<File>?) {
                        lst = t as ArrayList<File>
                        updateAdapterFileClassifiaction(lst, spr.isGridMode)
                    }

                })
        }

        fileRecentViewModel.getAllFile()
            .observe(this, object : Observer<List<FileRc>> {
                override fun onChanged(t: List<FileRc>?) {
                    lstRc = t as ArrayList<FileRc>
                }

            })
    }

    private fun getListBySingleExtension(extension: String) {
        fileViewModel = ViewModelUtils(this).initFileViewModel()
        fileRecentViewModel = ViewModelUtils(this).initFileRecentViewModel()

        fileViewModel.getAllFileByExtension(extension)
            .observe(this, object : Observer<List<File>> {
                override fun onChanged(t: List<File>?) {
                    lst = t as ArrayList<File>
                    updateAdapterFileClassifiaction(lst, spr.isGridMode)
                }

            })
    }

    private fun setupToolBar() {
        setSupportActionBar(toolbar_classification)
        val mActionBar = supportActionBar!!
        mActionBar.setTitle(extension)
        mActionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun checkExtension() {
        try {
            extension = intent.getStringExtra(ExtensionUltis.INTENT_EXTENSION)
//            if (extension.equals(ExtensionUltis.EPUB))
//                extension = "epub"
//            else if (extension.equals(ExtensionUltis.PDF))
//                extension = "pdf"
//            else if (extension.equals(ExtensionUltis.ZIP))
//                extension = "zip"
//            else if (extension.equals(ExtensionUltis.WORD))
//                extension = "doc"
//            else if (extension.equals(ExtensionUltis.ODT))
//                extension = "odt"
//            else if (extension.equals(ExtensionUltis.RTF))
//                extension = "rtf"
//            else if (extension.equals(ExtensionUltis.DJVU))
//                extension = "html"
//            else if (extension.equals(ExtensionUltis.TEXT))
//                extension = "txt"

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateAdapterFileClassifiaction(list: ArrayList<File>, isGrid: Boolean) {

        adapter = FileAdapter(this, list, isGrid)
        val layoutManager = object : GridLayoutManager(this, if (isGrid) 3 else 1) {
            override fun canScrollVertically(): Boolean {
                return true
            }
        }

        rv_file_classification.setHasFixedSize(true)
        rv_file_classification.isNestedScrollingEnabled = false
        rv_file_classification.layoutManager = layoutManager
        rv_file_classification.adapter = adapter
    }

    private fun startDocumentActivity(file: File) {
        checkRecent(file)
        val intent = Intent(this, DocumentActivity::class.java)
        // API>=21: intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) /* launch as a new document */
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.fromFile(java.io.File(file.path))
        startActivity(intent)
    }

    private fun startReadTextActivity(file: File) {
        checkRecent(file)
        val intent = Intent(this, ReadTextActivity::class.java)
        intent.putExtra(ExtensionUltis.INTENT_EXTENSION_TEXT, file.path)
        startActivity(intent)
    }

    private fun startFolioReader(path: String) {
//        val folioReader = FolioReader.get()
//        folioReader.openBook(path)
    }

    private fun startTTBSFileViewActivity(path: String) {
        Log.d(TAG, "Open File: " + path);
        TBSFileViewActivity.viewFile(this, java.io.File(path).absolutePath);

//            TBSFileViewActivity.viewFile(this, getFileByPath());

//            TBSFileViewActivity.viewFile(this, getFilePath("TestExcel.xls"));
    }

    private fun checkRecent(file: File) {
        var isRecent = false
        if (lstRc.size > 0) {
            lstRc.forEach {
                if (it.path.equals(file.path)) {
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
    }


    //    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_main, menu)
//        menuItem = menu.findItem(R.id.action_search)
//        searchView = menuItem.actionView as SearchView
//        searchView.setOnQueryTextListener(queryText)
//        searchView.queryHint = getString(R.string.search_file)
//        super.onCreateOptionsMenu(menu, inflater)
//    }


    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        menuItem = menu!!.findItem(R.id.action_search)
        searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(queryText)
        searchView.queryHint = getString(R.string.search_file)
        searchView.setBackgroundColor(Color.WHITE)
        return true
    }

    val queryText = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            val listSearch = lst
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
            lst.forEach {
                if (it.name!!.toLowerCase().contains(newText!!.toLowerCase())) {
                    arrayList.add(it)
                }
            }
            adapter.updateList(arrayList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.sort_name -> {
                sortByName(lst)
                adapter.updateList(lst)
            }
            R.id.sort_date -> {
                sortByDate(lst)
                adapter.updateList(lst)
            }
            R.id.sort_size -> {
                sortBySize(lst)
                adapter.updateList(lst)
            }
            R.id.display_list -> {
                if (spr.isGridMode) {
                    spr.isGridMode = false
                    updateAdapterFileClassifiaction(lst, spr.isGridMode)
                }
            }
            R.id.display_grid -> {
                if (!spr.isGridMode) {
                    spr.isGridMode = true
                    updateAdapterFileClassifiaction(lst, spr.isGridMode)
                }
            }


        }
        return true
    }

    public fun renameInList(file: File, newName: File): Boolean {
        try {
            val i = lst.indexOf(file)
            if (i != -1) {
                lst.set(i, newName)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    public fun updateData(file: File) {

        try {
            val i = lst.indexOf(file)
            if (i != -1) {
                adapter.updateItem(i, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


//    private fun showPopup(view:View) {
//        val popup =  PopupMenu(this, view)
//        try {
//            // Reflection apis to enforce show icon
//            val fields = popup.javaClass.getDeclaredFields()
//            fields.forEach {
//                if (it.getName().equals(POPUP_CONSTANT)) {
//                    it.setAccessible(true);
//                    val menuPopupHelper = it.get(popup)
//                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.getName())
//                    val setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, Boolean::class.java)
//                    setForceIcons.invoke(menuPopupHelper, true)
//                }
//            }
//        } catch (e:Exception) {
//            e.printStackTrace();
//        }
//        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
//        popup.setOnMenuItemClickListener(object :android.widget.PopupMenu.OnMenuItemClickListener,
//            PopupMenu.OnMenuItemClickListener {
//            override fun onMenuItemClick(item: MenuItem?): Boolean {
//
//                return true
//            }
//
//        });
//        popup.show();
//    }


    private fun sortByName(list: ArrayList<File>) {
        Collections.sort(list, object : Comparator<File> {
            override fun compare(o1: File?, o2: File?): Int {
                return o1!!.name!!.compareTo(o2!!.name!!)
            }

        })
    }

    private fun sortByDate(list: ArrayList<File>) {
        Collections.sort(list, object : Comparator<File> {
            override fun compare(o1: File?, o2: File?): Int {
                return o1!!.date!!.compareTo(o2!!.date!!)
            }

        })
    }

    private fun sortBySize(list: ArrayList<File>) {
        Collections.sort(list, object : Comparator<File> {
            override fun compare(o1: File?, o2: File?): Int {
                return o1!!.size!!.compareTo(o2!!.size!!)
            }

        })
    }

    private fun getFileByPath(): String {
        return java.io.File("/storage/emulated/0/Download/Tro Lai 30 Nam Truoc - Bong Lai Khach.epub")
            .absolutePath
    }

    @NonNull
    private fun getFilePath(fileName: String): String {
        return java.io.File(FILE_DIR + fileName).absolutePath
    }


}
