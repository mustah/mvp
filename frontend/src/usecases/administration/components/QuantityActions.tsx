import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import NavigationCheck from 'material-ui/svg-icons/navigation/check';
import NavigationClose from 'material-ui/svg-icons/navigation/close';
import * as React from 'react';
import {actionMenuItemIconStyle} from '../../../app/themes';
import {ButtonLink} from '../../../components/buttons/ButtonLink';
import {Row} from '../../../components/layouts/row/Row';
import {OnClickWithId} from '../../../types/Types';

interface Props {
  dataItem: any;
  confirmDelete: OnClickWithId;
  editAction: OnClickWithId;
  inEdit: boolean;
  saveAction: OnClickWithId;
  cancelAction: OnClickWithId;
}

export const QuantityActions = ({dataItem, confirmDelete, editAction, saveAction, cancelAction, inEdit}: Props) => {

  const onClickSave = () => {
    saveAction(dataItem);
  };
  const onClickCancel = () => {
    cancelAction(dataItem);
  };
  const onClickEdit = () => {
    editAction(dataItem);
  };
  const onClickDelete = () => {
    confirmDelete(dataItem);
  };
  return inEdit ?
    (
      <Row>
        <ButtonLink
          onClick={onClickSave}
          key={`save-${dataItem.dataIndex}`}
        >
          <NavigationCheck/>
        </ButtonLink>
        <ButtonLink
          onClick={onClickCancel}
          key={`cancel-${dataItem.dataIndex}`}
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
          key={`edit-${dataItem.dataIndex}`}
        >
          <ImageEdit style={actionMenuItemIconStyle}/>
        </ButtonLink>
        <ButtonLink
          onClick={onClickDelete}
          key={`delete-${dataItem.dataIndex}`}
        >
          <ActionDelete style={actionMenuItemIconStyle}/>
        </ButtonLink>
      </Row>
    )
    ;
};
