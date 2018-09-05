import FlatButton from 'material-ui/FlatButton';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import * as React from 'react';
import {colors} from '../../app/themes';
import {OnClickWithId, uuid} from '../../types/Types';

interface InfoLinkProps {
  onClick: OnClickWithId;
  id: uuid;
}

const DeleteIcon = <ActionDelete color={colors.blue}/>;

export const ButtonDelete = ({onClick, id}: InfoLinkProps) => {
  const deleteClick = () => onClick(id);

  return (
    <FlatButton
      hoverColor="inherit"
      icon={DeleteIcon}
      onClick={deleteClick}
    />
  );
};
