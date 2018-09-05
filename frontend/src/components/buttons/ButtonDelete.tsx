import FlatButton from 'material-ui/FlatButton';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import * as React from 'react';
import {OnClickWithId, uuid} from '../../types/Types';

interface InfoLinkProps {
  onClick: OnClickWithId;
  id: uuid;
}

export const ButtonDelete = ({onClick, id}: InfoLinkProps) => {
  const deleteClick = () => onClick(id);

  return (
    <FlatButton
      hoverColor="inherit"
      icon={<ActionDelete/>}
      onClick={deleteClick}
    />
  );
};
