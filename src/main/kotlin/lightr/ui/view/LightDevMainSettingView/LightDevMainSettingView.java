package lightr.ui.view.LightDevMainSettingView;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

/**
 * @author Lightr
 * @date 2025/6/17
 * @Description 描述
 */
public class LightDevMainSettingView implements Configurable {
    @Override
    public String getDisplayName() {
        return "LightDevs";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return new JPanel(); // 空面板
    }

    @Override
    public boolean isModified() { return false; }

    @Override
    public void apply() {}
}

