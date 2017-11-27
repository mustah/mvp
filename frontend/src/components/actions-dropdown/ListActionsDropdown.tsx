import Divider from 'material-ui/Divider';
import * as React from 'react';
import {routes} from '../../app/routes';
import {ActionsDropdown, menuItem, MenuItems} from './ActionsDropdown';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {IdNamed, OnClickWithId} from '../../types/Types';

interface Props {
  item: IdNamed;
  selectEntryAdd: OnClickWithId;
}

export const ListActionsDropdown = (props: Props) => {
  const {item, selectEntryAdd} = props;

  const noop = () => null;

  const menuItems: MenuItems = [
    menuItem({name: translate('export to Excel (.csv)'), onClick: noop}),
    menuItem({name: translate('export to JSON'), onClick: noop}),
    <Divider key={'something'}/>,
    menuItem({
      name: translate('add to report'),
      onClick: () => {
        history.push(`${routes.report}/${item.id}`);
        selectEntryAdd(item.id);
      },
    }),
  ];

  return (<ActionsDropdown menuItems={menuItems}/>);
};
