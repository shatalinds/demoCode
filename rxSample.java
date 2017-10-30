---------------------------------------------------------------------------------------------------
@Inject AppCustom mCustom;
.....
.....
@OnClick({R.id.rlTopFrame, R.id.cbAllComp, R.id.tvHeader})
public void topFrameClick(View view) {
   final String ownGroupId = mCustom.getOwnGroupId();
   tvHeader.setText(getString(R.string.choose_department));
   cbAllComp.setButtonDrawable(getResources().getDrawable(R.drawable.ic_arrow_up, null));

   if (TextUtils.isEmpty(ownGroupId)) {
        mBearerNetwork
                 .getService()
                 .editProfile4Business(mBearerNetwork.getUniquiId(), mBearerNetwork.getTSString())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribeOn(Schedulers.newThread())
                 .subscribe(responseBody -> this.onEditProfileResponse(responseBody),
                         throwable -> Timber.e(throwable));
      } else {
          EventBus.getDefault().post(new DepartMessageEvent());
      }
   }
---------------------------------------------------------------------------------------------------
