import Divider from 'material-ui/Divider';
import * as React from 'react';
import {routes} from '../../app/routes';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {IdNamed, OnClickWithId} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown, MenuItems} from './ActionsDropdown';

interface Props {
  item: IdNamed;
  selectEntryAdd: OnClickWithId;
}

export const ListActionsDropdown = ({item: {id}, selectEntryAdd}: Props) => {
  const noop = () => null;

  const onClick = () => {
    history.push(`${routes.report}/${id}`);
    selectEntryAdd(id);
  };

  const menuItems: MenuItems = [
    <ActionMenuItem name={translate('export to Excel (.csv)')} onClick={noop} key={`0-${id}`}/>,
    <ActionMenuItem name={translate('export to JSON')} onClick={noop} key={`1-${id}`}/>,
    <Divider key={`2-${id}`}/>,
    <ActionMenuItem name={translate('add to report')} onClick={onClick} key={`3-${id}`}/>,
  ];

  return (<ActionsDropdown menuItems={menuItems}/>);
};
