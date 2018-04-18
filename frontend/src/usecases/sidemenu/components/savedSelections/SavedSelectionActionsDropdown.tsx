import * as React from 'react';
import {ActionMenuItem} from '../../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../../components/actions-dropdown/ActionsDropdown';
import {translate} from '../../../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
}

export const SavedSelectionActionsDropdown = ({id, confirmDelete}: Props) => {
  const openAlert = () => confirmDelete(id);

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      openAlert();
    };
    return <ActionMenuItem name={translate('delete user selection')} onClick={onClickDelete} key={`1-${id}`}/>;
  };

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};
