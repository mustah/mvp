import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {ActionsDropdown, CallbackAction} from './ActionsDropdown';
import {history} from '../../../../index';
import {routes} from '../../../app/routes';

interface Props {
  item: IdNamed;
}

export const ListActionsDropdown = (props: Props) => {
  const {item} = props;

  const noop = () => 0;

  const actions: CallbackAction[] = [
    {name: translate('export to Excel (.csv)'), onClick: noop},
    {name: translate('export to JSON'), onClick: noop},
    {
      name: translate('add to report'),
      onClick: () => history.push(`${routes.report}/${item.id}`),
    },
  ];

  return (<ActionsDropdown actions={actions}/>);
};
