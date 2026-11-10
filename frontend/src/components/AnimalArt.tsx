import type { ReactNode, SVGProps } from 'react'

type Props = SVGProps<SVGSVGElement> & { size?: number }

function Base({ size = 96, children, ...props }: Props & { children: ReactNode }) {
  return (
    <svg
      viewBox="0 0 100 100"
      width={size}
      height={size}
      fill="none"
      aria-hidden="true"
      {...props}
    >
      {children}
    </svg>
  )
}

export function CaoCartoon(props: Props) {
  return (
    <Base {...props}>
      <ellipse cx="20" cy="46" rx="12" ry="22" transform="rotate(-18 20 46)" fill="#c98a4b" />
      <ellipse cx="80" cy="46" rx="12" ry="22" transform="rotate(18 80 46)" fill="#c98a4b" />
      <circle cx="50" cy="54" r="30" fill="#e0a96d" />
      <ellipse cx="50" cy="70" rx="18" ry="13" fill="#f7e3c8" />
      <circle cx="39" cy="48" r="4" fill="#3b2a1e" />
      <circle cx="61" cy="48" r="4" fill="#3b2a1e" />
      <circle cx="40.5" cy="46.5" r="1.4" fill="#fff" />
      <circle cx="62.5" cy="46.5" r="1.4" fill="#fff" />
      <ellipse cx="50" cy="63" rx="6" ry="4.5" fill="#3b2a1e" />
      <path d="M50 67v5" stroke="#3b2a1e" strokeWidth="2.4" strokeLinecap="round" />
      <path d="M43 72c2 5 5 7 7 7s5-2 7-7" stroke="#3b2a1e" strokeWidth="2.4" strokeLinecap="round" />
      <path d="M46 74c0 4 2 7 4 7s4-3 4-7z" fill="#ef7d8a" />
      <ellipse cx="27" cy="58" rx="5" ry="3.5" fill="#f0b6bd" opacity="0.6" />
      <ellipse cx="73" cy="58" rx="5" ry="3.5" fill="#f0b6bd" opacity="0.6" />
    </Base>
  )
}

export function GatoCartoon(props: Props) {
  return (
    <Base {...props}>
      <path d="M22 34l4-22 20 14z" fill="#8b8f98" />
      <path d="M78 34l-4-22-20 14z" fill="#8b8f98" />
      <path d="M27 32l2.5-13 11 8z" fill="#f2b8c6" />
      <path d="M73 32l-2.5-13-11 8z" fill="#f2b8c6" />
      <circle cx="50" cy="56" r="29" fill="#aab0ba" />
      <ellipse cx="50" cy="68" rx="16" ry="11" fill="#eef1f5" />
      <path d="M36 52c2-3 6-4 8-1" stroke="#3b2a1e" strokeWidth="2.6" strokeLinecap="round" />
      <path d="M64 52c-2-3-6-4-8-1" stroke="#3b2a1e" strokeWidth="2.6" strokeLinecap="round" />
      <circle cx="39" cy="55" r="4" fill="#3b2a1e" />
      <circle cx="61" cy="55" r="4" fill="#3b2a1e" />
      <path d="M50 64l-3 3h6z" fill="#ef7d8a" />
      <path d="M50 67v3" stroke="#3b2a1e" strokeWidth="2.2" strokeLinecap="round" />
      <path d="M44 69c2 3 4 4 6 4s4-1 6-4" stroke="#3b2a1e" strokeWidth="2.2" strokeLinecap="round" />
      <path d="M30 62h-14M30 67l-13 4M70 62h14M70 67l13 4" stroke="#8b8f98" strokeWidth="2" strokeLinecap="round" />
    </Base>
  )
}

export function CoelhoCartoon(props: Props) {
  return (
    <Base {...props}>
      <ellipse cx="38" cy="24" rx="7" ry="20" transform="rotate(-8 38 24)" fill="#f0e6dc" />
      <ellipse cx="62" cy="24" rx="7" ry="20" transform="rotate(8 62 24)" fill="#f0e6dc" />
      <ellipse cx="38" cy="25" rx="3" ry="13" transform="rotate(-8 38 25)" fill="#f2b8c6" />
      <ellipse cx="62" cy="25" rx="3" ry="13" transform="rotate(8 62 25)" fill="#f2b8c6" />
      <circle cx="50" cy="60" r="28" fill="#f7f1ea" />
      <circle cx="40" cy="56" r="4" fill="#3b2a1e" />
      <circle cx="60" cy="56" r="4" fill="#3b2a1e" />
      <circle cx="41.4" cy="54.6" r="1.3" fill="#fff" />
      <circle cx="61.4" cy="54.6" r="1.3" fill="#fff" />
      <path d="M50 65l-3 3h6z" fill="#ef7d8a" />
      <path d="M50 68v3" stroke="#3b2a1e" strokeWidth="2.2" strokeLinecap="round" />
      <path d="M44 70c2 3 4 4 6 4s4-1 6-4" stroke="#3b2a1e" strokeWidth="2.2" strokeLinecap="round" />
      <ellipse cx="28" cy="64" rx="5" ry="3.5" fill="#f2b8c6" opacity="0.5" />
      <ellipse cx="72" cy="64" rx="5" ry="3.5" fill="#f2b8c6" opacity="0.5" />
    </Base>
  )
}

export function AveCartoon(props: Props) {
  return (
    <Base {...props}>
      <path d="M62 40c14 2 22 12 30 10-6 10-16 14-26 12z" fill="#2f8f6f" />
      <ellipse cx="48" cy="56" rx="26" ry="30" fill="#45bf94" />
      <ellipse cx="42" cy="60" rx="14" ry="18" fill="#7bd8b4" />
      <path d="M46 30c0-6 6-10 10-8-2-6 4-10 8-8 0 6-2 10-6 12" stroke="#2f8f6f" strokeWidth="3" strokeLinecap="round" />
      <circle cx="52" cy="38" r="4" fill="#3b2a1e" />
      <circle cx="53.4" cy="36.6" r="1.3" fill="#fff" />
      <path d="M56 44l12 4-12 5z" fill="#f4a63a" />
      <path d="M40 80v8M48 82v6M56 80v8" stroke="#f4a63a" strokeWidth="3" strokeLinecap="round" />
    </Base>
  )
}
