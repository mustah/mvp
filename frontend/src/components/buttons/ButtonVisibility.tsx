import Checkbox from 'material-ui/Checkbox';
import ActionVisibility from 'material-ui/svg-icons/action/visibility';
import ActionVisibilityOff from 'material-ui/svg-icons/action/visibility-off';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

interface VisibilityProps extends Clickable, ThemeContext {
  checked?: boolean;
}

const HiddenIcon = <ActionVisibilityOff/>;
const VisibleIcon = <ActionVisibility/>;

export const ButtonVisibility = withCssStyles(({onClick, checked, cssStyles: {primary}}: VisibilityProps) => (
  <Checkbox
    checkedIcon={HiddenIcon}
    uncheckedIcon={VisibleIcon}
    onClick={onClick}
    iconStyle={{fill: primary.bg}}
    checked={checked}
    style={{width: 24, paddingTop: 4}}
  />
));
