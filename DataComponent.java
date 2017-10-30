import javax.inject.Singleton;
import dagger.Component;

/**
 * Инджектор зависимостей для {@link DataModule}
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */
@Singleton
@Component(modules = {AppModule.class, DataModule.class})
public interface DataComponent {

    void inject(SignUpActivity activity);

    void inject(RemindPasswordActivity activity);

    void inject(UsersFragment fragment);

    void inject(BaseDrawerActivity activity);

    void inject(EventsFragment fragment);

    void inject(SplashActivity activity);

    void inject(UserActivity activity);
}
