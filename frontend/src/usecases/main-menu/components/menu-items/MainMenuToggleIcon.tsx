import {default as classNames} from 'classnames';
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

export const MainMenuToggleIcon = ({onClick, isSideMenuOpen}: Props) => {
  const iconsProps: SvgIconProps = {
    style,
    onClick,
    color: colors.blueA900,
    hoverColor: colors.blueA700,
  };

  const renderArrow = isSideMenuOpen
    ? (<NavigationChevronLeft {...iconsProps}/>)
    : (<NavigationChevronRight {...iconsProps}/>);

  return (
    <RowRight className={classNames('MainMenuToggleIcon', {isSideMenuOpen})}>
      {renderArrow}
    </RowRight>
  );
};
