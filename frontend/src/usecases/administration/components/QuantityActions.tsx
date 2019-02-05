import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import NavigationCheck from 'material-ui/svg-icons/navigation/check';
import NavigationClose from 'material-ui/svg-icons/navigation/close';
import * as React from 'react';
import {actionMenuItemIconStyle} from '../../../app/themes';
import {ButtonLink} from '../../../components/buttons/ButtonLink';
import {Row} from '../../../components/layouts/row/Row';
import {OnClickWithId, uuid} from '../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
  editAction: OnClickWithId;
  inEdit: boolean;
  saveAction: OnClickWithId;
  cancelAction: OnClickWithId;
}

export const QuantityActions = ({id, confirmDelete, editAction, saveAction, cancelAction, inEdit}: Props) => {

  const onClickSave = () => {
    saveAction(id);
  };
  const onClickCancel = () => {
    cancelAction(id);
  };
  const onClickEdit = () => {
    editAction(id);
  };
  const onClickDelete = () => {
    confirmDelete(id);
  };
  return inEdit ?
    (
      <Row>
        <ButtonLink
          onClick={onClickSave}
          key={`save-${id}`}
        >
          <NavigationCheck/>
        </ButtonLink>
        <ButtonLink
          onClick={onClickCancel}
          key={`cancel-${id}`}
        >
          <NavigationClose/>
        </ButtonLink>
      </Row>
    )
    :
    (
      <Row>
        <ButtonLink
          onClick={onClickEdit}
          key={`edit-${id}`}
        >
          <ImageEdit style={actionMenuItemIconStyle}/>
        </ButtonLink>
        <ButtonLink
          onClick={onClickDelete}
          key={`delete-${id}`}
        >
          <ActionDelete style={actionMenuItemIconStyle}/>
        </ButtonLink>
      </Row>
    )
    ;
};
