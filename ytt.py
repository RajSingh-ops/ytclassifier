import sys
import io
from youtube_transcript_api import YouTubeTranscriptApi

# Force UTF-8 output on Windows
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def main():
    if len(sys.argv) < 2:
        print("Usage: python ytt.py <video_id>")
        sys.exit(1)

    video_id = sys.argv[1]

    ytt_api = YouTubeTranscriptApi()
    transcript = ytt_api.fetch(video_id)

    full_text = " ".join([entry.text for entry in transcript])
    print(full_text)

if __name__ == "__main__":
    main()
