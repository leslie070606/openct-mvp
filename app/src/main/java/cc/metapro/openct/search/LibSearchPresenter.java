package cc.metapro.openct.search;

/*
 *  Copyright 2016 - 2017 OpenCT open source class table
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.LibraryFactory;
import cc.metapro.openct.data.university.item.BookInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.base.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Keep
class LibSearchPresenter implements LibSearchContract.Presenter {

    private static final String TAG = LibSearchPresenter.class.getName();

    private LibSearchContract.View mLibSearchView;

    private Context mContext;

    private LibraryFactory mLibraryFactory;

    LibSearchPresenter(@NonNull LibSearchContract.View libSearchView, Context context) {
        mLibSearchView = libSearchView;
        mContext = context;
        mLibSearchView.setPresenter(this);
        mLibraryFactory = Loader.getLibrary(mContext);
    }

    @Override
    public Disposable search(final String type, final String content) {
        mLibSearchView.showOnSearching();

        final ProgressDialog progressDialog = ActivityUtils.getProgressDialog(mContext, R.string.searching_library);
        progressDialog.show();

        Observable<List<BookInfo>> observable = Observable.create(new ObservableOnSubscribe<List<BookInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookInfo>> e) throws Exception {
                Map<String, String> map = new HashMap<>(2);
                map.put(mContext.getString(R.string.key_search_type), type);
                map.put(mContext.getString(R.string.key_search_content), content);
                e.onNext(mLibraryFactory.search(map));
            }
        });

        Observer<List<BookInfo>> observer = new MyObserver<List<BookInfo>>(TAG) {
            @Override
            public void onNext(List<BookInfo> books) {
                progressDialog.dismiss();
                mLibSearchView.onSearchResult(books);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable nextPage() {
        mLibSearchView.showOnSearching();
        Observable<List<BookInfo>> observable = Observable.create(new ObservableOnSubscribe<List<BookInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookInfo>> e) throws Exception {
                e.onNext(mLibraryFactory.getNextPage());
            }
        });

        Observer<List<BookInfo>> observer = new MyObserver<List<BookInfo>>(TAG) {
            @Override
            public void onNext(List<BookInfo> books) {
                mLibSearchView.onNextPageResult(books);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public void start() {

    }
}
