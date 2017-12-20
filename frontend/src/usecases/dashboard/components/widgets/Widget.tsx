import Card from 'material-ui/Card/Card';
import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {colors, menuItemInnerDivStyle} from '../../../../app/themes';
import {IconMore} from '../../../../components/icons/IconMore';
import {Column} from '../../../../components/layouts/column/Column';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {PopoverMenu} from '../../../../components/popover/PopoverMenu';
import {Subtitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {Children} from '../../../../types/Types';
import './Widget.scss';

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
        <PopoverMenu IconComponent={IconMore}>
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
