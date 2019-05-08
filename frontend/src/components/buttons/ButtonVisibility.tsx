import Checkbox from 'material-ui/Checkbox';
import ActionVisibility from 'material-ui/svg-icons/action/visibility';
import ActionVisibilityOff from 'material-ui/svg-icons/action/visibility-off';
import * as React from 'react';
import {colors} from '../../app/colors';
import {Clickable} from '../../types/Types';

interface VisibilityProps extends Clickable {
  checked?: boolean;
}

const iconStyle: React.CSSProperties = {fill: colors.blueA700};

const HiddenIcon = <ActionVisibilityOff/>;
const VisibleIcon = <ActionVisibility/>;

export const ButtonVisibility = ({checked, onClick}: VisibilityProps) => (
  <Checkbox
    checkedIcon={HiddenIcon}
    uncheckedIcon={VisibleIcon}
    onClick={onClick}
    iconStyle={iconStyle}
    checked={checked}
    style={{width: 24, paddingTop: 4}}
  />
);
