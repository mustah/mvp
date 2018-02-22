import * as React from 'react';
import {translate} from '../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
}

export const OrganisationActionsDropdown = ({id, confirmDelete}: Props) => {

  const openAlert = () => confirmDelete(id);

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      openAlert();
    };
    return [
      (
        <ActionMenuItem name={translate('delete organisation')} onClick={onClickDelete} key={`1-${id}`}/>
      ),
    ];
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
