import * as React from 'react';
import {history} from '../../../../index';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {routes} from '../../../app/routes';
import {ActionsDropdown, menuItem, MenuItems} from './ActionsDropdown';
import Divider from 'material-ui/Divider';

interface Props {
  item: IdNamed;
}

export const ListActionsDropdown = (props: Props) => {
  const {item} = props;

  const noop = () => 0;

  const menuItems: MenuItems = [
    menuItem({name: translate('export to Excel (.csv)'), onClick: noop}),
    menuItem({name: translate('export to JSON'), onClick: noop}),
    <Divider key={'something'}/>,
    menuItem({
      name: translate('add to report'),
      onClick: () => history.push(`${routes.report}/${item.id}`),
    }),
  ];

  return (<ActionsDropdown menuItems={menuItems}/>);
};
