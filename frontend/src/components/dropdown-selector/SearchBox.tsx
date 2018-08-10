import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';

interface SearchBoxProps {
  value: string;
  onUpdateSearch: (event: any) => void;
}

const searchStyle: React.CSSProperties = {
  position: 'absolute',
  right: 12,
  top: 7,
  color: '#7b7b7b',
};

export const SearchBox = ({value, onUpdateSearch}: SearchBoxProps) => (
  <div className="SearchBox">
    <input type="textfield" className="SearchBox-input" value={value} onChange={onUpdateSearch}/>
    <ActionSearch style={searchStyle}/>
  </div>
);
