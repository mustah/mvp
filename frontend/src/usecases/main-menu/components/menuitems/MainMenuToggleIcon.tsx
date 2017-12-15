import * as classNames from 'classnames';
import {SvgIconProps} from 'material-ui';
import NavigationChevronLeft from 'material-ui/svg-icons/navigation/chevron-left';
import NavigationChevronRight from 'material-ui/svg-icons/navigation/chevron-right';
import * as React from 'react';
import {colors, iconStyle} from '../../../../app/themes';
import {RowRight} from '../../../../components/layouts/row/Row';
import {OnClick} from '../../../../types/Types';
import './MainMenuToggleIcon.scss';

interface Props {
  onClick: OnClick;
  isSideMenuOpen: boolean;
}

const style: React.CSSProperties = {
  ...iconStyle,
  cursor: 'pointer',
};

export const MainMenuToggleIcon = (props: Props) => {
  const {onClick, isSideMenuOpen} = props;
  const iconsProps: SvgIconProps = {
    style,
    onClick,
    color: colors.darkBlue,
    hoverColor: colors.blue,
  };

  return (
    <RowRight className={classNames('MainMenuToggleIcon', {isSideMenuOpen})}>
      {isSideMenuOpen ? (<NavigationChevronLeft {...iconsProps}/>) : (<NavigationChevronRight {...iconsProps}/>)}
    </RowRight>
  );
};
