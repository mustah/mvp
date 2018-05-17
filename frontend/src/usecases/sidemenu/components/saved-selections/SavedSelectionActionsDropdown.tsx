import * as React from 'react';
import {ActionMenuItem} from '../../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../../components/actions-dropdown/ActionsDropdown';
import {translate} from '../../../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../../../types/Types';

interface Props {
  id: uuid;
  openConfirmDialog: OnClickWithId;
}

export const SavedSelectionActionsDropdown = ({id, openConfirmDialog}: Props) => {
  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      openConfirmDialog(id);
    };
    return <ActionMenuItem name={translate('delete user selection')} onClick={onClickDelete} key={`1-${id}`}/>;
  };

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};
