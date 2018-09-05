import Checkbox from 'material-ui/Checkbox';
import ActionVisibility from 'material-ui/svg-icons/action/visibility';
import ActionVisibilityOff from 'material-ui/svg-icons/action/visibility-off';
import * as React from 'react';
import {colors} from '../../app/themes';
import {OnClickWithId, uuid} from '../../types/Types';

interface VisibilityProps {
  onClick: OnClickWithId;
  id: uuid;
}

const iconStyle: React.CSSProperties = {fill: colors.blue};

const HiddenIcon = <ActionVisibilityOff/>;
const VisibleIcon = <ActionVisibility/>;

export const ButtonVisibility = ({onClick, id}: VisibilityProps) => {
  const toggleClick = () => onClick(id);

  return (
    <Checkbox
      checkedIcon={HiddenIcon}
      uncheckedIcon={VisibleIcon}
      onClick={toggleClick}
      iconStyle={iconStyle}
    />
  );
};
