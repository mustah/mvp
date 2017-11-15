import Card from 'material-ui/Card/Card';
import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import 'Widget.scss';
import {translate} from '../../../../services/translationService';
import {Children} from '../../../../types/Types';
import {colors, menuItemInnerDivStyle} from '../../../app/themes';
import {Column} from '../../../common/components/layouts/column/Column';
import {RowMiddle} from '../../../common/components/layouts/row/Row';
import {PopoverMenu} from '../../../common/components/popover/PopoverMenu';
import {Subtitle} from '../../../common/components/texts/Titles';

interface Props {
  title?: string;
  children: Children;
}

const deleteStyle: React.CSSProperties = {
  ...menuItemInnerDivStyle,
  color: colors.red,
};

export const Widget = (props: Props) => {
  const {title, children} = props;

  return (
    <Card className="Widget">
      <RowMiddle className="space-between">
        <Subtitle className="Widget-subtitle">{title}</Subtitle>
        <PopoverMenu>
          <MenuItem style={menuItemInnerDivStyle} className="first-uppercase">
            {translate('edit')}
          </MenuItem>
          <MenuItem style={deleteStyle} className="first-uppercase">
            {translate('delete')}
          </MenuItem>
        </PopoverMenu>
      </RowMiddle>
      <Column className="Content">
        {children}
      </Column>
    </Card>
  );
};
