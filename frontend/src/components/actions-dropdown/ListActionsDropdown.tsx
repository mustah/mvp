import * as React from 'react';
import {routes} from '../../app/routes';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {IdNamed, OnClick, OnClickWithId, RenderFunction} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

interface Props {
  item: IdNamed;
  selectEntryAdd: OnClickWithId;
}

export const ListActionsDropdown = ({item: {id}, selectEntryAdd}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onAddToReport = () => {
      onClick();
      history.push(`${routes.report}/${id}`);
      selectEntryAdd(id);
    };
    return ([
      <ActionMenuItem name={translate('add to report')} onClick={onAddToReport} key={`3-${id}`}/>,
    ]);
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
