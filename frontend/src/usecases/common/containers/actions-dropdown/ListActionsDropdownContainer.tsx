import Divider from 'material-ui/Divider';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {history} from '../../../../index';
import {translate} from '../../../../services/translationService';
import {IdNamed, uuid} from '../../../../types/Types';
import {routes} from '../../../app/routes';
import {selectEntryAdd} from '../../../report/reportActions';
import {ActionsDropdown, menuItem, MenuItems} from '../../components/actions-dropdown/ActionsDropdown';

interface Props {
  item: IdNamed;
}

interface DispatchToProps {
  selectEntryAdd: (id: uuid) => void;
}

const ListActionsDropdown = (props: Props & DispatchToProps) => {
  const {item} = props;

  const noop = () => null;

  const menuItems: MenuItems = [
    menuItem({name: translate('export to Excel (.csv)'), onClick: noop}),
    menuItem({name: translate('export to JSON'), onClick: noop}),
    <Divider key={'something'}/>,
    menuItem({
      name: translate('add to report'),
      onClick: () => {
        history.push(`${routes.report}/${item.id}`);
        props.selectEntryAdd(item.id);
      },
    }),
  ];

  return (<ActionsDropdown menuItems={menuItems}/>);
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  selectEntryAdd,
}, dispatch);

export const ListActionsDropdownContainer =
  connect<{}, DispatchToProps, Props>(null, mapDispatchToProps)(ListActionsDropdown);
