import Checkbox from 'material-ui/Checkbox';
import ActionVisibility from 'material-ui/svg-icons/action/visibility';
import ActionVisibilityOff from 'material-ui/svg-icons/action/visibility-off';
import * as React from 'react';
import {OnClickWithId, uuid} from '../../types/Types';

interface VisibilityProps {
  onClick: OnClickWithId;
  id: uuid;
}

export const ButtonVisibility = ({onClick, id}: VisibilityProps) => {
  const toggleClick = () => {
    onClick(id);
  };

  return (
    <Checkbox
      checkedIcon={<ActionVisibilityOff/>}
      uncheckedIcon={<ActionVisibility/>}
      onClick={toggleClick}
    />
  );
};
