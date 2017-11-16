import * as React from 'react';
import {translate} from '../../../../../services/translationService';
import {ActionsDropdown, CallbackAction} from '../../actions-dropdown/ActionsDropdown';
import {Column} from '../../layouts/column/Column';
import {TabUnderline} from './TabUnderliner';

export const TabSettings = () => {
  const noop = () => 0;

  const actions: CallbackAction[] = [
    {name: translate('export to Excel (.csv)'), onClick: noop},
    {name: translate('export to JSON'), onClick: noop},
  ];

  return (
    <Column className="TabSettings">
      <ActionsDropdown actions={actions} className="flex-1 Row-right"/>
      <TabUnderline/>
    </Column>
  );
};
